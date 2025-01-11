package dev.osunolimits.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.osunolimits.main.App;
import dev.osunolimits.main.WebServer;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import lombok.Data;
import spark.Request;
import spark.Response;
import spark.Route;

public class ShiinaDocs {
    public static ArrayList<DocsModel> docs = new ArrayList<>();

    @Data
    public class DocsModel {
        private String filename;
        private String content;
        private String icon;
        private String route;
        private String krz;
        private String navbar;
        private String title;
        private String footer;
        private String behindLogin;
    }

    public static DocsModel extractComments(String markdown) {
        DocsModel model = new ShiinaDocs().new DocsModel();
        String[] lines = markdown.split("\n");
        if (lines.length == 0 || lines.length < 5)
            return null;
        model.icon = lines[1].substring(5);
        model.route = lines[2].substring(6);
        model.krz = lines[3].substring(6);
        model.title = lines[4].substring(6);
        model.navbar = lines[5].substring(7);
        model.footer = lines[6].substring(7);
        model.behindLogin = lines[7].substring(12);
        return model;
    }

    private String readDocsFile(String fileName) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get("docs/" + fileName));
        StringBuilder sb = new StringBuilder();
        for (String line : allLines) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    public void initializeDocs() {

        File[] files = new File("docs/").listFiles();
        Parser parser = Parser.builder().build();
        for (File file : files) {
            if (file.getName().endsWith("md")) {
                try {
                    String sb = readDocsFile(file.getName());
                    DocsModel model = ShiinaDocs.extractComments(sb);
                    if (model == null) {
                        App.log.warn("File docs/" + file.getName() + " does not contain comments");
                        continue;
                    }

                    Node document = parser.parse(sb);
                    HtmlRenderer renderer = HtmlRenderer.builder().build();

                    model.content = renderer.render(document);
                    model.filename = file.getName();
                    docs.add(model);
                } catch (IOException e) {
                    App.log.warn("Failed to read file docs/" + file.getName());
                }
            } else {
                App.log.warn("File docs/" + file.getName() + " is not a markdown file");
            }
        }
        WebServer.get("/docs/:route", getDocsRoute());
    }

    private Route getDocsRoute() {

        return new Shiina() {
            @Override
            public Object handle(Request req, Response res) throws Exception {
                ShiinaRequest shiina = new ShiinaRoute().handle(req, res);

                String route = req.params(":route");
                for (DocsModel model : docs) {
                    if (model.route.equals(route)) {
                        shiina.data.put("actNav", 5);
                        shiina.data.put("title", model.title);
                        shiina.data.put("icon", model.icon);
                        shiina.data.put("krz", model.krz);
                        shiina.data.put("content", model.content);
                        shiina.data.put("navbar", model.navbar);
                        shiina.data.put("footer", model.footer);
                        shiina.data.put("behindLogin", model.behindLogin);

                        if (model.behindLogin.equals("true") && !shiina.loggedIn) {
                            return redirect(res, shiina, "/login?path=" + req.pathInfo());
                        }

                        return renderTemplate("docs.html", shiina, res, req);
                    }
                }
                return notFound(res, shiina);
            }
        };
    }

    public void watchDirectory() throws Exception {

        new Thread(() -> {
            Parser parser = Parser.builder().build();
            try {
                Logger log = (Logger) LoggerFactory.getLogger(ShiinaDocs.class);
                log.info("Watching dir docs/ for changes");
                Path directoryPath = Paths.get("docs");
                WatchService watchService = FileSystems.getDefault().newWatchService();

                directoryPath.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();
                    System.out.println();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (!event.context().toString().endsWith(".md")) {
                            continue;
                        }
                        DocsModel model = null;
                        String sb = "";
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE
                                || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            sb = readDocsFile(event.context().toString());
                            model = ShiinaDocs.extractComments(sb);
                            if (model == null) {
                                log.warn("File docs/" + event.context() + " does not contain comments");
                                continue;
                            }
                            model.filename = event.context().toString();

                        }
                        log.info(event.kind().name() + " | docs/" + event.context());
                        try {
                            // Handle the specific event
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                boolean exists = false;
                                for (DocsModel doc : docs) {
                                    if (doc.route.equals(model.route)) {
                                        exists = true;
                                        log.warn("Route " + model.route + " already exists");
                                        continue;
                                    }
                                }
                                if (!exists)
                                    docs.add(model);
                            } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                for (DocsModel doc : docs) {
                                    if (doc.filename.equals(event.context())) {
                                        docs.remove(doc);
                                        log.warn("Removed " + model.route + "");
                                        continue;
                                    }
                                }

                            } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {

                                for (DocsModel doc : docs) {
                                    if (doc.filename.equals(model.filename)) {
                                        readDocsFile(event.context().toString());
                                        doc.icon = model.icon;
                                        doc.krz = model.krz;
                                        doc.title = model.title;
                                        doc.navbar = model.navbar;
                                        doc.footer = model.footer;
                                        doc.behindLogin = model.behindLogin;
                                        doc.filename = event.context().toString();
                                        
                                        Node document = parser.parse(sb);
                                        HtmlRenderer renderer = HtmlRenderer.builder().build();
                                        doc.content = renderer.render(document);
                                        doc.route = model.route;

                                        continue;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // TODO: handle exception
                        }

                    }

                    // To receive further events, reset the key
                    key.reset();
                }
            } catch (IOException e) {
                App.log.error("Failed to watch directory");
            } catch (InterruptedException e) {
                App.log.error("Interuppted while watching directory");
            }
        }).run();
        ;

    }

}
