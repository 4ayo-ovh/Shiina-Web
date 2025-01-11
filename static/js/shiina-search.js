var inSearch = false;

function searchShiina(val) {   
    let shiinaSearch = document.getElementById('shiina-search');
    if (val == null || val == '') {
        shiinaSearch.classList.add('d-none');
        return;
    }

    if(!inSearch) {
        inSearch = true;
        
        if(shiinaSearch.classList.contains('d-none')) {
            shiinaSearch.classList.remove('d-none');
        }
        
        fetch("/api/v1/search?query=" + val)
        .then(response => response.json())
        .then(data => {
            shiinaSearch.innerHTML = "";
            data.forEach((item) => {
                const resultContainer = document.createElement('div');
                resultContainer.classList.add('search-result');

                const link = document.createElement('a');
                link.href = getResultUrl(item);
                link.classList.add('d-flex', 'align-items-start', 'text-decoration-none');

                if (item.source_table === 'maps') {
                    const img = document.createElement('img');
                    img.src = "/api/v1/thumb?setId=" + item.set_id;
                    img.alt = item.item_name + ' Image';
                    img.classList.add('me-2');
                    img.style.width = '60px';
                    img.style.height = '60px';
                    img.style.objectFit = 'cover';
                    link.appendChild(img);
                } else if (item.source_table === 'users') {
                    const img = document.createElement('img');
                    img.src = avatarServer + "/" + item.id;
                    img.alt = item.item_name + ' Avatar';
                    img.classList.add('me-2');
                    img.style.width = '60px';
                    img.style.height = '60px';
                    img.style.objectFit = 'cover';
                    link.appendChild(img);
                }

                const contentDiv = document.createElement('div');
                const title = document.createElement('h5');
                title.textContent = item.item_name;
                title.classList.add('mb-1');

                const typeLabel = document.createElement('p');
                typeLabel.textContent = 'Type: ' + capitalizeFirstLetter(item.source_table);
                typeLabel.classList.add('mb-0', 'text-muted');

                contentDiv.appendChild(title);
                contentDiv.appendChild(typeLabel);

                link.appendChild(contentDiv);
                resultContainer.appendChild(link);
                shiinaSearch.appendChild(resultContainer);
            });
            updateTooltips();
        })
        .catch(error => console.error('Error:', error));

        inSearch = false;
    }
}

function getResultUrl(item) {
    switch(item.source_table) {
        case 'users':
            return "/u/" + item.id;
        case 'maps':
            return "/b/" + item.id;
        case 'clans':
            return "/clan/" + item.id;
        default:
            return "#";
    }
}

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}