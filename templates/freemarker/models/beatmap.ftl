<div class="col-12 col-lg-<#if bigBm??>12 h-100<#else>6</#if> flex-column">
    <div class="card beatmap-card text-white bg-secondary h-100">
        <div class="row g-0 h-100">
            <div class="col-md-4 d-flex flex-grow-1">
                <div class="image-placeholder">
                    <div class="spinner-grow" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
                <img src="https://assets.ppy.sh/beatmaps/${beatmap.set_id?c}/covers/card.jpg"
                    class="img-fluid rounded-start card-img" alt="Beatmap Image"
                    onload="loadLazyLoadImage(this);"
                    onerror="lazyLoadNoImage(this, '${noBeatmapIcon}');">
            </div>
            <div class="col-md-8">
                <div class="card-body d-flex flex-column justify-content-between h-100">
                    <div>
                        <div class="card-title-bm">
                            <span class="fs-5">
                                ${beatmap.filename?replace(".osu", "")}
                            </span>
                        </div>
                        <div class="card-artist">
                            <span class="card-text fw-light">by ${beatmap.artist}
                            </span>
                        </div>
                        <div class="card-creator">
                            <small class="card-text">mapped by ${beatmap.creator}
                            </small>
                        </div>
                        <div class="my-2">
                            <span class="text-muted">
                                <i class="fa-solid fa-star"></i>
                                ${beatmap.diff?string("0.00")}
                                <i class="fa-solid fa-play"></i>
                                ${beatmap.plays}
                                <i class="fa-solid fa-circle-check"></i>
                                ${beatmap.passes}
                                <i class="fa-solid fa-calendar"></i>
                                <span data-timestamp-format="date" data-timestamp="${beatmap.last_update}">
                                    ${beatmap.last_update}
                                </span>
                                <span class="badge bg-${convertStatusBackColor(beatmap.status)}">
                                    ${convertStatusBack(beatmap.status)}
                                </span>
                            </span>
                        </div>
                    </div>
                    <div class="hover-icons mt-2">
                        <a href="/b/${beatmap.id?c}" class="icon-link">
                            <i data-bs-toggle="tooltip" data-bs-placement="top" title='View Beatmap'  class="fas fa-eye"></i>
                        </a>
                        <a href="https://osu.direct/api/d/${beatmap.set_id?c}" class="icon-link">
                            <i data-bs-toggle="tooltip" data-bs-placement="top" title='Download'  class="fa-solid fa-download"></i>
                        </a>
                        <a href="osu://dl/${beatmap.set_id?c}" class="icon-link">
                            <i data-bs-toggle="tooltip" data-bs-placement="top" title='Download with osu!Direct' class="fa-solid fa-play-circle"></i>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>