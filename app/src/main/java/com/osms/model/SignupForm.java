package com.osms.model;

/**
 * Values typed into the sign up forms. The customer form uses the name, phone and birth date fields,
 * the seller form uses the store fields. Lengths follow the Users, Customers and Sellers tables.
 */
public class SignupForm {

    private String username = "";
    private String email = "";
    private String password = "";
    private String confirmPassword = "";
    private String firstName = "";
    private String lastName = "";
    private String phone = "";
    private String birthDate = "";
    private String storeName = "";
    private String taxNumber = "";

    /** A copy without the passwords, so they are never sent back to the page. */
    public SignupForm withoutPasswords() {
        SignupForm copy = new SignupForm();
        copy.username = username;
        copy.email = email;
        copy.firstName = firstName;
        copy.lastName = lastName;
        copy.phone = phone;
        copy.birthDate = birthDate;
        copy.storeName = storeName;
        copy.taxNumber = taxNumber;
        return copy;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
}
