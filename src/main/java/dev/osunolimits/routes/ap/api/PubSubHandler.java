package dev.osunolimits.routes.ap.api;

import java.io.File;

import com.google.gson.Gson;

import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.modules.utils.AuditLogger;
import dev.osunolimits.modules.utils.UserInfoCache;
import dev.osunolimits.plugins.events.OnAddDonorEvent;
import dev.osunolimits.plugins.events.OnRankBeatmapEvent;
import dev.osunolimits.routes.ap.api.PubSubModels.*;
import dev.osunolimits.utils.osu.PermissionHelper;
import spark.Request;
import spark.Response;

public class PubSubHandler extends Shiina {
    private final Gson GSON;

    public PubSubHandler() {
        this.GSON = new Gson();
    }

    public enum MessageType {
        RANK,
        RESTRICT,
        UNRESTRICT,
        WIPE,
        ALERT_ALL,
        GIVEDONATOR,
        ADDPRIV,
        REMOVEPRIV,
        NONE,
        RMPB
    }

    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);

        if (shiina.user == null) {
            return redirect(res, shiina, "/login");
        }

        if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.STAFF)) {
            return redirect(res, shiina, "/");
        }

        MessageType type = MessageType.NONE;
        if (req.queryParams("type") != null) {
            try {
                type = MessageType.valueOf(req.queryParams("type").toUpperCase());
            } catch (Exception e) {
                type = MessageType.NONE;
            }
        }

        if (type == MessageType.NONE) {
            shiina.mysql.close();
            return "invalid type";
        }

        PubSubModels models = new PubSubModels();
        AuditLogger auditLogger = new AuditLogger(shiina.mysql, type);
        
        switch (type) {
            case RANK: {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.NOMINATOR)) {
                    return redirect(res, shiina, "/");
                }
                RankOutput rankOutput = models.new RankOutput();
                rankOutput.setBeatmap_id(Integer.parseInt(req.queryParams("beatmap_id")));
                rankOutput.setStatus(Integer.parseInt(req.queryParams("status")));
                rankOutput.setFrozen(Boolean.parseBoolean(req.queryParams("frozen")));
                auditLogger.rankMap(shiina.user, rankOutput.getBeatmap_id(), rankOutput.getStatus());
                
                new OnRankBeatmapEvent(rankOutput.beatmap_id, rankOutput.status, rankOutput.frozen, shiina.user.id).callListeners();;
                
                App.jedisPool.publish("rank", GSON.toJson(rankOutput));
                break;
            }
            case RESTRICT: {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.MODERATOR)) {
                    return redirect(res, shiina, "/");
                }
                RestrictInput restrictInput = models.new RestrictInput();
                restrictInput.setId(Integer.parseInt(req.queryParams("id")));
                restrictInput.setUserId(shiina.user.id);
                restrictInput.setReason(req.queryParams("reason"));
                auditLogger.restrictUser(shiina.user, restrictInput.getId(), restrictInput.getReason());
                App.jedisPool.publish("restrict", GSON.toJson(restrictInput));
                break;
            }
            case UNRESTRICT: {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.MODERATOR)) {
                    return redirect(res, shiina, "/");
                }
                UnrestrictInput unrestrictInput = models.new UnrestrictInput();
                unrestrictInput.setId(Integer.parseInt(req.queryParams("id")));
                unrestrictInput.setUserId(shiina.user.id);
                unrestrictInput.setReason(req.queryParams("reason"));
                auditLogger.unrestrictUser(shiina.user, unrestrictInput.getId(), unrestrictInput.getReason());

                App.jedisPool.publish("unrestrict", GSON.toJson(unrestrictInput));
                break;
            }
            case WIPE:
            if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.MODERATOR)) {
                return redirect(res, shiina, "/");
            }
                WipeInput wipeInput = models.new WipeInput();
                wipeInput.setId(Integer.parseInt(req.queryParams("id")));
                wipeInput.setMode(Integer.parseInt(req.queryParams("mode")));
                String reason = req.queryParams("reason");
                auditLogger.wipeUser(shiina.user, wipeInput.getId(), wipeInput.getMode(), reason);

                App.jedisPool.publish("wipe", GSON.toJson(wipeInput));
                break;
            case ALERT_ALL: {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.MODERATOR)) {
                    return redirect(res, shiina, "/");
                }
                AlertAllInput alertAllInput = models.new AlertAllInput();
                alertAllInput.setMessage(req.queryParams("message"));
                auditLogger.alertAll(shiina.user, alertAllInput.getMessage());

                App.jedisPool.publish("alert_all", GSON.toJson(alertAllInput));
                break;
            }
            case GIVEDONATOR: {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.ADMINISTRATOR)) {
                    return redirect(res, shiina, "/");
                }
                GiveDonatorInput giveDonatorInput = models.new GiveDonatorInput();
                giveDonatorInput.setId(Integer.parseInt(req.queryParams("id")));
                giveDonatorInput.setDuration(req.queryParams("duration"));
                auditLogger.giveDonator(shiina.user, giveDonatorInput.getId(), giveDonatorInput.getDuration());
                UserInfoCache cache = new UserInfoCache();
                cache.reloadUser(giveDonatorInput.getId());

                new OnAddDonorEvent(giveDonatorInput.getDuration(), giveDonatorInput.getId(), shiina.user.id).callListeners();
                
                App.jedisPool.publish("givedonator", GSON.toJson(giveDonatorInput));
                break;
            }
            case ADDPRIV: {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.ADMINISTRATOR)) {
                    return redirect(res, shiina, "/");
                }
                AddPrivInput addPrivInput = models.new AddPrivInput();
                addPrivInput.setId(Integer.parseInt(req.queryParams("id")));
                addPrivInput.setPrivs(req.queryParamsValues("privs"));
                auditLogger.addPriv(shiina.user, addPrivInput.getId(), addPrivInput.getPrivs());

                App.jedisPool.publish("addpriv", GSON.toJson(addPrivInput));
                break;
            }
            case REMOVEPRIV: {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.ADMINISTRATOR)) {
                    return redirect(res, shiina, "/");
                }
                RemovePrivInput removePrivInput = models.new RemovePrivInput();
                removePrivInput.setId(Integer.parseInt(req.queryParams("id")));
                removePrivInput.setPrivs(req.queryParamsValues("privs"));

                auditLogger.removePriv(shiina.user, removePrivInput.getId(), removePrivInput.getPrivs());

                App.jedisPool.publish("removepriv", GSON.toJson(removePrivInput));
                break;
            }

            case RMPB: {
                if (!PermissionHelper.hasPrivileges(shiina.user.priv, PermissionHelper.Privileges.MODERATOR)) {
                    return redirect(res, shiina, "/");
                }
                int userId = Integer.parseInt(req.queryParams("id"));
                String reasonPb = req.queryParams("reason");
                File avatarDir = new File(App.env.get("AVATARFOLDER"));
                File avatar = new File(avatarDir, userId + ".png");
                if (avatar.exists()) {
                    avatar.delete();
                }
                File avatarJpg = new File(avatarDir, userId + ".jpg");
                if (avatarJpg.exists()) {
                    avatarJpg.delete();
                }
                auditLogger.removeProfilePicture(shiina.user, userId, reasonPb);
                break;
            }

            default:
                break;
        }

        shiina.mysql.close();
        res.status(200);
        return "success";
    }
}
