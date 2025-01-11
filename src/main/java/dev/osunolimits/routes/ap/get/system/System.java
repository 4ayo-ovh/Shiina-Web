package dev.osunolimits.routes.ap.get.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dev.osunolimits.common.Database;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.routes.api.get.GetBmThumbnail;
import dev.osunolimits.utils.osu.PermissionHelper;
import spark.Request;
import spark.Response;

public class System extends Shiina {

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 10);

        if (!shiina.loggedIn) {
            return redirect(res, shiina, "/");
        }

        if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.ADMINISTRATOR)) {
            return redirect(res, shiina, "/");
        }

        // System and application statistics
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        // System statistics
        shiina.data.put("os_name", osBean.getName());
        shiina.data.put("os_version", osBean.getVersion());
        shiina.data.put("available_processors", osBean.getAvailableProcessors());
        shiina.data.put("system_load_average", osBean.getSystemLoadAverage());


        HashMap<String, byte[]> thumbnailCache = GetBmThumbnail.thumbnailCache;
        AtomicInteger thumbnailCacheSizeBytes = new AtomicInteger(0);
        thumbnailCache.forEach((k, v) -> {
            thumbnailCacheSizeBytes.addAndGet(v.length);
        });

        shiina.data.put("shiina_thumbnails", thumbnailCacheSizeBytes.get() / 1024 / 1024 + " MB (" + thumbnailCache.size() + " thumbnails)");


        // JVM memory usage
        shiina.data.put("jvm_total_memory", runtime.totalMemory());
        shiina.data.put("jvm_free_memory", runtime.freeMemory());
        shiina.data.put("jvm_max_memory", runtime.maxMemory());

        shiina.data.put("sql_con", Database.currentConnections);
        shiina.data.put("sql_con_list", Database.runningConnections.size());

        // Thread statistics
        shiina.data.put("thread_count", threadMXBean.getThreadCount());
        shiina.data.put("peak_thread_count", threadMXBean.getPeakThreadCount());
        shiina.data.put("daemon_thread_count", threadMXBean.getDaemonThreadCount());

        return renderTemplate("ap/system/system.html", shiina, res, req);
    }
}
