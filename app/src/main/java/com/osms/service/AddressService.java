package com.osms.service;

import com.osms.model.AddressForm;
import com.osms.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
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
    public void updateAddress(int customerId, int addressId, AddressForm form) {
        Values v = clean(form);
        if (!addressRepository.belongsToCustomer(addressId, customerId)) {
            throw new CustomerRequestException("This address was not found");
        }
        if (form.isDefaultAddress()) {
            addressRepository.clearDefault(customerId);
        }
        addressRepository.update(addressId, customerId, v.title, v.country, v.city, v.district, v.addressLine,
                v.postalCode, form.isDefaultAddress());
    }

    /** An address that an order ships to cannot be deleted (the Orders table refers to it). */
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
        String postalCode = text(form.getPostalCode(), "Postal code", 10, false);
        return new Values(
                text(form.getTitle(), "Title", 50, true),
                text(form.getCountry(), "Country", 50, true),
                text(form.getCity(), "City", 50, true),
                text(form.getDistrict(), "District", 50, true),
                text(form.getAddressLine(), "Address", 255, true),
                postalCode.isEmpty() ? null : postalCode);
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
