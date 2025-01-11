package dev.osunolimits.routes.ap.get.system;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.utils.osu.PermissionHelper;
import lombok.Data;
import spark.Request;
import spark.Response;

public class SystemThreads extends Shiina {

    @Data
    public class PublicThread {
        private String name;
        private long id;
        private long cpuTime;
        private long userTime;
        private long blockedCount;
        private long blockedTime;
        private long waitedCount;
        private long waitedTime;
        private long lockOwnerId;
        private String lockOwnerName;
        private boolean inNative;
        private boolean suspended;
        private boolean daemon;
        private boolean interrupted;
        private String state;
        private long stackTraceHash;
    }

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

        List<PublicThread> threads = new ArrayList<>();

        Thread.getAllStackTraces().forEach((thread, stackTrace) -> {
            PublicThread publicThread = new PublicThread();
            publicThread.setName(thread.getName());
            publicThread.setId(thread.threadId());
            publicThread.setCpuTime(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadCpuTime(thread.threadId()) : -1);
            publicThread.setUserTime(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadUserTime(thread.threadId()) : -1);
            publicThread.setBlockedCount(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId()).getBlockedCount() : -1);
            publicThread.setBlockedTime(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId()).getBlockedTime() : -1);
            publicThread.setWaitedCount(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId()).getWaitedCount() : -1);
            publicThread.setWaitedTime(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId()).getWaitedTime() : -1);
            publicThread.setLockOwnerId(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId()).getLockOwnerId() : -1);
            publicThread.setLockOwnerName(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId()).getLockOwnerName() : null);
            publicThread.setInNative(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId()).isInNative() : false);
            publicThread.setSuspended(thread.isAlive() ? ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId()).isSuspended() : false);
            publicThread.setDaemon(thread.isAlive() ? thread.isDaemon() : false);
            publicThread.setInterrupted(thread.isAlive() ? thread.isInterrupted() : false);
            publicThread.setState(thread.isAlive() ? thread.getState().name() : "TERMINATED");
            publicThread.setStackTraceHash(Arrays.hashCode(stackTrace));
            threads.add(publicThread);
        });

        shiina.data.put("threads", threads);

        return renderTemplate("ap/system/threads.html", shiina, res, req);
    }
}
