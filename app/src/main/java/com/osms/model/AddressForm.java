package com.osms.model;

/** Values typed into the address form. Lengths follow the Addresses table. */
public class AddressForm {

    private String title = "";
    private String country = "Turkey";
    private String city = "";
    private String district = "";
    private String addressLine = "";
    private String postalCode = "";
    private boolean defaultAddress;

    public static AddressForm from(AddressView address) {
        AddressForm form = new AddressForm();
        form.title = address.title();
        form.country = address.country();
        form.city = address.city();
        form.district = address.district();
        form.addressLine = address.addressLine();
        form.postalCode = address.postalCode() == null ? "" : address.postalCode();
        form.defaultAddress = address.defaultAddress();
        return form;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
}
