let tooltipList = []; // Keep track of initialized tooltips
var out;
var turnstileId = null;

const bsPrimary = getComputedStyle(document.documentElement).getPropertyValue('--bs-primary').trim();

document.addEventListener("turbo:visit", (event) => {
    const content = document.getElementById("wrapper");

    content.classList.add("fade-out");

    event.preventDefault();

    content.addEventListener('transitionend', () => {
        Turbo.visit(event.detail.url);
    });
});

document.addEventListener("turbo:render", () => {
    const content = document.getElementById("wrapper");

    // Remove the exit animation and trigger an entry animation
    content.classList.remove("fade-out");
    content.classList.add("fade-in");

    setTimeout(() => {
        content.classList.remove("fade-in");
    }, 300); // Match the animation duration
});

loadEventTurbo = document.addEventListener("turbo:load", function () {
    loadUserPage();

    const nodesWithTimestamp = document.querySelectorAll('[data-timestamp]');
    const nodesArray = Array.from(nodesWithTimestamp);

    let relNodes = document.querySelectorAll('[data-shiina-call]');
    const relArray = Array.from(relNodes);

    handleRelUpdate(relArray);

    loadContrySelectorIfPresent();
    loadTurnstileIfPresent();
    
    if (document.getElementById('video') != undefined) {
        const video = document.getElementById('video');
        loadVideoWithDelay(video);
    }

    initUserpageEditor();

    nodesArray.forEach(node => {
        let format;
        if (node.getAttribute('data-timestamp-format') == 'date') { format = false; } else { format = true; }
        node.innerHTML = timeUntil(node.getAttribute('data-timestamp'), format);
    });

    updateTooltips();
});

function initUserpageEditor() {
    if(document.getElementById('editor') != undefined) {
        if(document.getElementsByClassName('wysibb').length == 0) {
            var wbbOpt = {
                buttons: "bold,italic,underline,|,img,link,|,table,removeFormat",
            };
            $("#editor").wysibb(wbbOpt);
        }
    }
}

function loadContrySelectorIfPresent() {
    if (document.getElementById('country-selector') != undefined) {
        $('#country-selector').select2({
            templateResult: formatCountry,
            templateSelection: formatCountry,
            minimumResultsForSearch: Infinity // Hide search if unnecessary
        });

        function formatCountry(country) {
            // Ensure country.id is valid
            if (!country.id) {
                return country.text;
            }

            // Get the flag URL
            var flagUrl = $(country.element).data('flag');
            var countryName = country.text;

            // Create the HTML using jQuery parseHTML or native DOM
            var $countryHtml = $(
                '<span class="text-white">' +
                '<img src="' + flagUrl + '" style="width: 20px; height: 15px; margin-right: 8px;">' +
                countryName +
                '</span>'
            );

            return $countryHtml;
        }
    }
}

function handleRelUpdate(relArray) {
    relArray.forEach(node => {
        let user = node.getAttribute('data-user');
        node.onclick = function () {
            fetch("/api/v1/update_rel?u="+user) 
            .then(response => response.text())
            .then(response => {
                Turbo.visit(window.location.href);
            })
        }
    });
}

submitStartTurbo = document.addEventListener('turbo:submit-start', function () {
    Turbo.navigator.delegate.adapter.showProgressBar();
});

beforeVisitTurbo = document.addEventListener('turbo:before-visit', () => {
    
});

beforeRenderTurbo = document.addEventListener('turbo:before-render', () => {
   
});

winEvent = document.addEventListener('resize', function () {
    if (out) {
        out.resize();
    }
});

// Remove tooltips and event listeners when navigating back
unloadEventTurbo = document.addEventListener("turbo:before-cache", function () {
    Turbo.navigator.delegate.adapter.showProgressBar();

    // Properly dispose of all tooltips before caching
    tooltipList.forEach(tooltip => {
        tooltip.dispose();
    });
    tooltipList = [];

    if (out != null) {
        out.destroy();
        out = null; // Clear reference
    }

    unloadTurnstileIfPresent();

    // Clean up event listeners
    window.removeEventListener('resize', winEvent);
    document.removeEventListener("turbo:load", loadEventTurbo);
    document.removeEventListener("turbo:before-cache", unloadEventTurbo);
    document.removeEventListener("turbo:submit-start", submitStartTurbo);
    document.removeEventListener("turbo:before-visit", beforeVisitTurbo);
    document.removeEventListener("turbo:before-render", beforeRenderTurbo);
});

function unloadTurnstileIfPresent() {
    if(turnstileId != null && document.getElementById("turnstile")) {
        if(document.getElementById("turnstile").innerHTML.length > 0) {
            console.log("Removing Turnstile");
            turnstile.remove(turnstileId);
        }
    }
}

function loadTurnstileIfPresent() {
    if (document.getElementById("turnstile")) {
        turnstile.ready(function () {
        if (document.getElementById("turnstile").innerHTML.length == 0) {
            console.log("Rendering Turnstile");
            try {
                turnstileId = turnstile.render("#turnstile", {
                sitekey: turnstileToken,
                callback: function (token) {
                },
            });
            } catch (error) {
                
            }
        }
    });
    }
}

function loadFirstPlaces(apiUrl, firstLoad = true) {
    let offset = document.getElementById('offsetFirstPlaces');

    if(firstLoad) {
        offset.value = 0;
    }
    
    apiUrl += offset.value;

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
           
            let container = document.getElementById('firstPlaces');
            let countElement = document.getElementById('firstPlacesCount');
            
            countElement.innerHTML = '(' + data.count + ')';
            if(!data.hasNextPage) {
                let btn = document.getElementById('firstPlacesButton');
                btn.classList.add('disabled');
            }

            if(firstLoad)
                container.innerHTML = '';
            data.firstPlaces.forEach(firstPlace => {
                // Create a new element for each first place
                let element = document.createElement('div');
                element.innerHTML = loadScorePanel(firstPlace.grade, firstPlace.map_id, firstPlace, firstPlace.pp, firstPlace.acc, firstPlace.max_combo, firstPlace.play_time, firstPlace.map_name, firstPlace.map_set_id, firstPlace.score_id,firstPlace.mods);
                container.appendChild(element);
            });
            updateTooltips();
        })
        .catch(error => console.error('Error:', error));
}


function loadBestScores(apiUrl, firstLoad = true) {
    let offset = document.getElementById('offsetBestScores');

    if(firstLoad) {
        offset.value = 0;
    }
    
    apiUrl += offset.value + '&scope=best';

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
           
            let container = document.getElementById('bestScores');
    
            if(!data.hasNextPage) {
                let btn = document.getElementById('bestScoresButton');
                btn.classList.add('disabled');
            }

            if(firstLoad)
                container.innerHTML = '';
            data.scores.forEach(firstPlace => {
     
                let element = document.createElement('div');
                element.innerHTML = loadScorePanel(firstPlace.grade, firstPlace.map_id, firstPlace, firstPlace.pp, firstPlace.acc, firstPlace.max_combo, firstPlace.play_time, firstPlace.map_name, firstPlace.map_set_id, firstPlace.score_id,firstPlace.mods, firstPlace.weight, firstPlace.weight_pp);
                container.appendChild(element);
            });
            updateTooltips();
        })
        .catch(error => console.error('Error:', error));
}


function loadLastScores(apiUrl, firstLoad = true) {
    let offset = document.getElementById('offsetLastScores');

    if(firstLoad) {
        offset.value = 0;
    }
    
    apiUrl += offset.value + '&scope=recent';

    fetch(apiUrl)
        .then(response => response.json())
        .then(data => {
           
            let container = document.getElementById('lastScores');
    
            if(!data.hasNextPage) {
                let btn = document.getElementById('lastScoresButton');
                btn.classList.add('disabled');
            }

            if(firstLoad)
                container.innerHTML = '';
            data.scores.forEach(firstPlace => {
     
                let element = document.createElement('div');
                element.innerHTML = loadScorePanel(firstPlace.grade, firstPlace.map_id, firstPlace, firstPlace.pp, firstPlace.acc, firstPlace.max_combo, firstPlace.play_time, firstPlace.map_name, firstPlace.map_set_id, firstPlace.score_id,firstPlace.mods);
                container.appendChild(element);
            });
            updateTooltips();
        })
        .catch(error => console.error('Error:', error));
}

function updateTooltips() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}


function loadLazyLoadImage(img) {
    if (img.complete && img.naturalHeight !== 0) {
        img.classList.add('loaded');
        img.previousElementSibling.style.display = 'none';
    } else {
        setTimeout(() => {
            img.classList.add('loaded');
            img.previousElementSibling.style.display = 'none';
        }, 300);
    }
}

function loadVideoWithDelay(video) {
    const loader = document.getElementById('spinner');
    if (!video.classList.contains('loaded')) {
        video.style.dislay = 'none';
        loader.style.display = 'block';
        setTimeout(() => {
            video.classList.add('loaded');
            loader.style.display = 'none';
            video.style.display = 'block';
        }, 3000);
    }
}


function lazyLoadNoImage(img, icon) {
    if (img.complete && img.naturalHeight !== 0) {
        img.classList.add('loaded');
        img.previousElementSibling.style.display = 'none';
    } else {
        random = Math.floor(Math.random() * 1000);
        setTimeout(() => {
            img.src = icon;
            img.classList.add('loaded');
            img.previousElementSibling.style.display = 'none';
        }, random);
    }
}

function timeUntil(dateInput, unix) {
    let inputDate;
    if (unix) {
        inputDate = new Date(dateInput * 1000);
    } else {
        inputDate = new Date(dateInput.replace(' ', 'T') + 'Z');
    }

    const currentDate = new Date();
    const differenceInSeconds = Math.floor((inputDate - currentDate) / 1000);

    const isPast = differenceInSeconds < 0;
    const absDifferenceInSeconds = Math.abs(differenceInSeconds);

    const timeUnits = [
        { unit: 'year', seconds: 29030400 },
        { unit: 'month', seconds: 2419200 },
        { unit: 'week', seconds: 604800 },
        { unit: 'day', seconds: 86400 },
        { unit: 'hour', seconds: 3600 },
        { unit: 'minute', seconds: 60 },
        { unit: 'second', seconds: 1 }
    ];

    let result = '';

    for (const { unit, seconds } of timeUnits) {
        const value = Math.floor(absDifferenceInSeconds / seconds);
        if (value > 0) {
            result += `${value} ${unit}${value > 1 ? 's' : ''} `;
            break;
        }
    }

    return isPast ? result.trim() + ' ago' : result.trim() + ' from now';
}

function removeParam(param) {
    let url = new URL(window.location.href);
    url.searchParams.delete(param);
    Turbo.visit(url);
}


function selectParam(param, value) {
    let url = new URL(window.location.href);
    url.searchParams.set(param, value);
    if (param != 'page' || param == 'country') {
        url.searchParams.set('page', 1);
    }
    if (param == 'mode') {
        url.searchParams.delete('country');
    }
    Turbo.visit(url);
}

function loadUserPage() {
    let loadedNew = document.getElementById('firstLoad');
    if(loadedNew == null) return;

    if(loadedNew.value == 'true') {
        loadedNew.value = 'false';
         // Load score panel first
        loadFirstPlaces(reqUrl);
        loadBestScores(reqUrlScores);
        loadLastScores(reqUrlScores);
    }

    initPlayCountGraph();
}

function initPlayCountGraph() {
    if (data.length > 0 && values.length > 0) {
        let ctx = document.getElementById('myChart');

        if (out) {
            out.destroy(); // Destroy previous instance if it exists
        }

        out = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data,
                datasets: [{
                    label: '# of Plays',
                    data: values,
                    borderColor: bsPrimary,
                    borderWidth: 2,
                    tension: 0.1,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                aspectRatio: 5,
                scales: {
                    x: {
                        grid: {
                            color: bootstrapTextTransparent
                        },
                        ticks: {
                            color: bootstrapTextColor
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: bootstrapTextTransparent
                        },
                        ticks: {
                            color: bootstrapTextColor
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }});
    }
}

function loadScorePanel(grade, mapId, score, pp, acc, maxCombo, playTime, name, setId, scoreId, mods, weight = 0, weight_pp = 0) {
    let output = '';

    output += '<div class="col col-12 act-entry d-flex flex-column mb-1">';

    // Score container
    output += '<div class="score-container bg-secondary score-panel d-flex flex-grow-1 position-relative" style="border-radius: 5px;">';
    output += '<div class="d-block d-lg-flex flex-grow-1">';

    beatmapImg = '/api/v1/thumb?setId=' + setId;

    // Beatmap cover image
    output += '<div class="col-12 col-lg-3 d-flex justify-content-center">';
    output += '<img src="' + beatmapImg + '" style="object-fit: cover; height: 100%;" class="img-fluid rounded-2 w-100" alt="">';
    output += '</div>';

    // Score details
    output += '<div class="col-12 d-flex p-2 mt-2 mt-lg-0 col-lg-9 mx-2 d-flex flex-column justify-content-start justify-content-sm-between">';
    output += '<span class="ms-2 text-wrap">' + name.replace('.osu', '');

    if (mods.length > 0) {
        output += ' <span class="fw-bold">+ ' + mods.join(",") + '</span>';
    }

    output += '</span>';
    output += '<span class="fs-5 ms-2">' + pp + 'pp <span class="fs-6">(' + acc + '%)</span> ';

    // Check for grade 'f' and display the Font Awesome F, otherwise show the grade image
    if (grade.toLowerCase() === 'f') {
        output += '<i class="fas fa-f fs-4 ms-1" style="height: 25px;"></i>';
    } else {
        output += '<img src="/img/ranking/ranking-' + grade + '.png" alt="Grade" class="img-fluid me-1" style="height: 30px;">';
    }
    if (weight > 0) {
        output += '<span class="fs-6 float-end me-4">weighted ' + weight + '% (' + weight_pp + 'pp)</span>';
    }

    output += '</span></div>';

    // Icons for view and replay
    output += '<div class="icon-container-score d-flex align-items-center">';
    output += '<a href="/scores/' + scoreId + '" class="icon-link-score me-3"><i data-bs-toggle="tooltip" data-bs-placement="top" title="View Score" class="fas fa-eye"></i></a>';
    output += '<a href="' + apiUrl + '/v1/get_replay?id=' + scoreId + '" class="icon-link-score"><i data-bs-toggle="tooltip" data-bs-placement="top" title="Download Replay" class="fas fa-download"></i></a>';
    output += '</div>';

    output += '</div></div></div>';

    return output;
}

function getBootstrapTextColor() {
    const element = document.getElementById('text');
    const computedStyle = window.getComputedStyle(element);
    return computedStyle.color;
}

function getBootstrapTextTransparent() {
    let color = getBootstrapTextColor();
    color = color.slice(0, -1) + ', 0.2)'; // Remove last char and add transparency
    return color;
}

function addLoader(button) {
    button.disabled = true; // Disable the button
    button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Loading...';
}

function removeLoader(button, originalText) {
    button.disabled = false; // Re-enable the button
    button.innerHTML = originalText; // Restore the original text
}

function loadMore() {
    let button = document.getElementById('firstPlacesButton');
    let originalText = button.innerHTML;
    addLoader(button); // Add loader to button

    let offset = document.getElementById('offsetFirstPlaces');
    offset.value = parseInt(offset.value) + 5;

    // Keep the loader visible for at least 1 second
    setTimeout(() => {
        loadFirstPlaces(reqUrl, false); // Call the loading function

        // Remove loader after loading process (1 second delay before executing)
        removeLoader(button, originalText);
    }, 500); // 1-second delay
}

function loadMoreScores() {
    let button = document.getElementById('bestScoresButton');
    let originalText = button.innerHTML;
    addLoader(button); // Add loader to button

    let offset = document.getElementById('offsetBestScores');
    offset.value = parseInt(offset.value) + 5;

    // Keep the loader visible for at least 1 second
    setTimeout(() => {
        loadBestScores(reqUrlScores, false); // Call the loading function

        // Remove loader after loading process (1 second delay before executing)
        removeLoader(button, originalText);
    }, 500); // 1-second delay
}

function loadMoreScoresLast() {
    let button = document.getElementById('lastScoresButton');
    let originalText = button.innerHTML;
    addLoader(button); // Add loader to button

    let offset = document.getElementById('offsetLastScores');
    offset.value = parseInt(offset.value) + 5;

    // Keep the loader visible for at least 1 second
    setTimeout(() => {
        loadLastScores(reqUrlScores, false); // Call the loading function

        // Remove loader after loading process (1 second delay before executing)
        removeLoader(button, originalText);
    }, 500); // 1-second delay
}
