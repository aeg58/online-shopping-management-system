(function () {
    const city = document.querySelector('[data-address-city]');
    const district = document.querySelector('[data-address-district]');
    const place = document.querySelector('[data-address-place]');
    const postal = document.querySelector('[data-address-postal]');
    if (!city || !district || !place) {
        return;
    }
    const cache = {};
    let current = null;

    function reset(select, label) {
        select.replaceChildren(new Option(label, ''));
    }

    function placeLabel(p) {
        return p[0] + (p[2] === 1 ? ' Köyü' : ' Mah.');
    }

    async function loadCity() {
        const option = city.selectedOptions[0];
        const id = option ? option.dataset.id : '';
        if (!id) {
            current = null;
            return null;
        }
        if (!cache[id]) {
            const response = await fetch('/data/tr/p' + id + '.json');
            if (!response.ok) {
                throw new Error('Address list could not be loaded');
            }
            cache[id] = await response.json();
        }
        current = cache[id];
        return current;
    }

    function fillPlaces() {
        reset(place, 'Choose a neighborhood');
        postal.value = '';
        const d = current && current.districts.find(x => x.name === district.value);
        if (!d) {
            return;
        }
        for (const p of d.places) {
            const option = new Option(placeLabel(p), placeLabel(p));
            option.dataset.postal = p[1];
            place.add(option);
        }
    }

    city.addEventListener('change', async () => {
        reset(district, 'Choose a district');
        reset(place, 'Choose a neighborhood');
        postal.value = '';
        district.disabled = true;
        try {
            const data = await loadCity();
            if (data) {
                for (const d of data.districts) {
                    district.add(new Option(d.name, d.name));
                }
            }
        } finally {
            district.disabled = false;
        }
    });

    district.addEventListener('change', async () => {
        if (!current) {
            await loadCity();
        }
        fillPlaces();
    });

    place.addEventListener('change', () => {
        const option = place.selectedOptions[0];
        postal.value = option && option.dataset.postal ? option.dataset.postal : '';
    });
})();
