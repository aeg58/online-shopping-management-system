(function () {
    'use strict';

    function money(amount) {
        return amount.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2}) + ' ₺';
    }

    function toCents(value) {
        return Math.round(parseFloat(value) * 100);
    }

    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.dataset.confirm)) {
                event.preventDefault();
            }
        });
    });

    document.querySelectorAll('[data-live-quantity]').forEach(function (input) {
        var total = input.form.querySelector('[data-live-total]');
        var unitCents = toCents(input.dataset.unitPrice);
        input.addEventListener('input', function () {
            var quantity = parseInt(input.value, 10);
            total.textContent = quantity > 0 ? money(unitCents * quantity / 100) : '—';
        });
    });

    document.querySelectorAll('form[data-live-cart]').forEach(function (form) {
        var row = form.closest('tr');
        var input = form.querySelector('input[name="quantity"]');
        var error = row.querySelector('[data-cart-error]');
        var timer;
        form.querySelector('[data-update-button]').hidden = true;

        function save() {
            fetch(form.action, {
                method: 'POST',
                headers: {'X-Requested-With': 'fetch'},
                body: new URLSearchParams(new FormData(form))
            }).then(function (response) {
                return response.json().then(function (data) {
                    if (!response.ok) {
                        error.textContent = data.error;
                        return;
                    }
                    error.textContent = '';
                    row.querySelector('[data-line-total]').textContent = data.lineTotal;
                    document.querySelector('[data-cart-total]').textContent = data.total;
                });
            }).catch(function () {
                error.textContent = 'The cart could not be updated. Please try again.';
            });
        }

        input.addEventListener('input', function () {
            clearTimeout(timer);
            if (input.value === '') {
                return;
            }
            timer = setTimeout(save, 400);
        });
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            clearTimeout(timer);
            save();
        });
    });

    document.querySelectorAll('form[data-live-checkout]').forEach(function (form) {
        var coupon = form.querySelector('[name="couponCode"]');
        var company = form.querySelector('[name="shippingCompanyId"]');
        var couponError = form.querySelector('[data-coupon-error]');
        var timer;

        function refresh() {
            var query = new URLSearchParams({shippingCompanyId: company.value, couponCode: coupon.value});
            fetch(form.dataset.summaryUrl + '?' + query, {headers: {'X-Requested-With': 'fetch'}})
                .then(function (response) {
                    return response.json();
                })
                .then(function (data) {
                    ['subtotal', 'discount', 'shipping', 'total'].forEach(function (key) {
                        document.querySelector('[data-summary="' + key + '"]').textContent = data[key];
                    });
                    couponError.textContent = data.couponError;
                })
                .catch(function () {
                    couponError.textContent = '';
                });
        }

        company.addEventListener('change', refresh);
        coupon.addEventListener('input', function () {
            clearTimeout(timer);
            timer = setTimeout(refresh, 400);
        });
    });
})();
