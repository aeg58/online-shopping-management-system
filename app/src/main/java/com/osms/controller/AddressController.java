package com.osms.controller;

import com.osms.model.AddressForm;
import com.osms.model.SessionUser;
import com.osms.repository.AddressRepository;
import com.osms.service.AddressService;
import com.osms.service.TurkeyAddressService;
import com.osms.service.CustomerRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/shop/addresses")
public class AddressController {

    private static final String REDIRECT_TO_LIST = "redirect:/shop/addresses";

    private final AddressRepository addressRepository;
    private final AddressService addressService;
    private final TurkeyAddressService turkey;

    public AddressController(AddressRepository addressRepository, AddressService addressService,
                             TurkeyAddressService turkey) {
        this.addressRepository = addressRepository;
        this.addressService = addressService;
        this.turkey = turkey;
    }

    @GetMapping
    public String addresses(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        model.addAttribute("addresses", addressRepository.findByCustomer(user.getCustomerId()));
        return "shop/addresses";
    }

    @GetMapping("/new")
    public String newAddress(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new AddressForm());
        }
        model.addAttribute("addressId", null);
        addChoices(model);
        return "shop/address-form";
    }

    private void addChoices(Model model) {
        AddressForm form = (AddressForm) model.getAttribute("form");
        addressService.prepare(form);
        model.addAttribute("provinces", turkey.provinces());
        model.addAttribute("districts", turkey.districts(form.getCity()));
        model.addAttribute("places", turkey.places(form.getCity(), form.getDistrict()));
        model.addAttribute("postalCode", turkey.place(form.getCity(), form.getDistrict(), form.getNeighborhood())
                .map(TurkeyAddressService.Place::postalCode).orElse(""));
    }

    @PostMapping
    public String add(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                      @ModelAttribute AddressForm form,
                      RedirectAttributes redirectAttributes) {
        try {
            addressService.addAddress(user.getCustomerId(), form);
            redirectAttributes.addFlashAttribute("successMessage", "Address added");
            return REDIRECT_TO_LIST;
        } catch (CustomerRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/shop/addresses/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                       @PathVariable int id,
                       Model model) {
        var address = addressRepository.findForCustomer(id, user.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", AddressForm.from(address));
        }
        model.addAttribute("addressId", id);
        addChoices(model);
        return "shop/address-form";
    }

    @PostMapping("/{id}")
    public String update(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                         @PathVariable int id,
                         @ModelAttribute AddressForm form,
                         RedirectAttributes redirectAttributes) {
        if (!addressRepository.belongsToCustomer(id, user.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        try {
            boolean newRow = addressService.updateAddress(user.getCustomerId(), id, form);
            redirectAttributes.addFlashAttribute("successMessage", newRow
                    ? "Address saved. Your earlier orders keep the address they were sent to."
                    : "Address saved");
            return REDIRECT_TO_LIST;
        } catch (CustomerRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/shop/addresses/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                         @PathVariable int id,
                         RedirectAttributes redirectAttributes) {
        try {
            addressService.deleteAddress(user.getCustomerId(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Address deleted");
        } catch (CustomerRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return REDIRECT_TO_LIST;
    }
}
