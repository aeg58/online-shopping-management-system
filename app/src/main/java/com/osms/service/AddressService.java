package com.osms.service;

import com.osms.model.AddressForm;
import com.osms.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private static final int MAX_LINE = 255;

    private final AddressRepository addressRepository;
    private final TurkeyAddressService turkey;

    public AddressService(AddressRepository addressRepository, TurkeyAddressService turkey) {
        this.addressRepository = addressRepository;
        this.turkey = turkey;
    }

    public void prepare(AddressForm form) {
        turkey.province(form.getCity()).ifPresent(p -> form.setCity(p.name()));
        turkey.district(form.getCity(), form.getDistrict()).ifPresent(d -> form.setDistrict(d.name()));
        if (form.getNeighborhood().isBlank()) {
            turkey.splitLine(form.getCity(), form.getDistrict(), form.getStreet()).ifPresent(parts -> {
                form.setNeighborhood(parts[0]);
                form.setStreet(parts[1]);
            });
        } else {
            turkey.place(form.getCity(), form.getDistrict(), form.getNeighborhood())
                    .ifPresent(p -> form.setNeighborhood(p.label()));
        }
    }

    @Transactional
    public void addAddress(int customerId, AddressForm form) {
        Values v = clean(form);
        boolean makeDefault = form.isDefaultAddress() || addressRepository.findByCustomer(customerId).isEmpty();
        if (makeDefault) {
            addressRepository.clearDefault(customerId);
        }
        addressRepository.insert(customerId, v.title, v.country, v.city, v.district, v.addressLine, v.postalCode,
                makeDefault);
    }

    @Transactional
    public boolean updateAddress(int customerId, int addressId, AddressForm form) {
        Values v = clean(form);
        if (!addressRepository.belongsToCustomer(addressId, customerId)) {
            throw new CustomerRequestException("This address was not found");
        }
        if (form.isDefaultAddress()) {
            addressRepository.clearDefault(customerId);
        }
        if (!addressRepository.isUsedByOrder(addressId)) {
            addressRepository.update(addressId, customerId, v.title, v.country, v.city, v.district, v.addressLine,
                    v.postalCode, form.isDefaultAddress());
            return false;
        }
        addressRepository.deactivate(addressId, customerId);
        addressRepository.insert(customerId, v.title, v.country, v.city, v.district, v.addressLine, v.postalCode,
                form.isDefaultAddress());
        return true;
    }

    @Transactional
    public void deleteAddress(int customerId, int addressId) {
        if (!addressRepository.belongsToCustomer(addressId, customerId)) {
            throw new CustomerRequestException("This address was not found");
        }
        if (addressRepository.deleteUnused(addressId, customerId) == 0) {
            throw new CustomerRequestException("This address is used by an order, so it cannot be deleted");
        }
    }

    private record Values(String title, String country, String city, String district, String addressLine,
                          String postalCode) {
    }

    private Values clean(AddressForm form) {
        String title = text(form.getTitle(), "Title", 50, true);
        TurkeyAddressService.Match match = turkey.match(form.getCity(), form.getDistrict(), form.getNeighborhood())
                .orElseThrow(() -> new CustomerRequestException(
                        "Please choose the city, district and neighborhood from the lists"));
        String street = text(form.getStreet(), "Street and building number", MAX_LINE, true);
        String line = TurkeyAddressService.plain(match.place().label()) + ", " + street;
        if (line.length() > MAX_LINE) {
            throw new CustomerRequestException("Street and building number is too long");
        }
        String postalCode = match.place().postalCode();
        return new Values(title, "Turkey",
                TurkeyAddressService.plain(match.province().name()),
                TurkeyAddressService.plain(match.district().name()),
                line,
                postalCode.isBlank() ? null : postalCode);
    }

    private String text(String value, String label, int maxLength, boolean required) {
        String clean = value == null ? "" : value.trim();
        if (required && clean.isEmpty()) {
            throw new CustomerRequestException(label + " is required");
        }
        if (clean.length() > maxLength) {
            throw new CustomerRequestException(label + " can have at most " + maxLength + " characters");
        }
        return clean;
    }
}
