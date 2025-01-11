<div class="col col-12 col-md-6 act-entry <#if (index >= 4)> d-none</#if> d-flex flex-column mb-4">
    <#if isActivity??>
    <div class="row p-2 align-items-center">
        <div class="col-auto">
            <img class="flag" src="${avatarServer}/${score.id?c}" alt="">
        </div>
        <div class="col-auto d-flex align-items-center">
            <span class="h6">
                ${score.name}
            </span>
            <small class="ms-2" data-timestamp-format="date" data-timestamp="${score.playTime}">
                ${score.playTime}
            </small>
        </div>
    </div>
    </#if>

    <#assign name = score.mapFilename?replace(".osu", "")>

    <div class="score-container bg-secondary score-panel d-flex flex-grow-1 position-relative" style="border-radius: 5px;">
        <div class="d-block d-lg-flex flex-grow-1">
            <div class="col-12 col-lg-3 d-flex justify-content-center">
                <img style="object-fit: cover; height: 100%;" class="img-fluid rounded-2 w-100" src="/api/v1/thumb?setId=${score.set_id?c}" alt="${name}">
            </div>
            <div class="col-12 d-flex p-2 mt-2 mt-lg-0 col-lg-7 mx-2 d-flex flex-column justify-content-start justify-content-sm-between">
                <#assign passedMods=score.mods>
                <span class="ms-2 text-wrap">
                    ${name}
                    <#include "/freemarker/modconvert.ftl">
                </span>
                <span class="fs-5 ms-2">
                    ${score.pp?string("0")}pp <span class="fs-6">(${score.acc?string("0.00")}%)</span> <#assign height=30> <#include "/freemarker/gradeconvert.ftl">
                </span>
            </div>
            <div class="icon-container-score d-flex align-items-center">
                <a href="/scores/${score.score_id?c}" class="icon-link-score me-3"><i data-bs-toggle="tooltip" data-bs-placement="top" title='View Score' class="fas fa-eye"></i></a>
                <a href="${apiUrlPub}/v1/get_replay?id=${score.score_id?c}" class="icon-link-score"><i data-bs-toggle="tooltip" data-bs-placement="top" title='Download Replay' class="fas fa-download"></i></a>
            </div>
        </div>
    </div>
</div>