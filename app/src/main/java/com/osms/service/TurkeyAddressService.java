package com.osms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Province, district and neighborhood/village names of Türkiye (static/data/tr, from TurkiyeAPI, MIT).
 * Address forms offer only these places, and the server checks every submitted address against them.
 * The Addresses columns are VARCHAR, so names are saved without Turkish letters, like the sample data
 * ("Cankaya", "Istanbul"); matching ignores those letters too.
 */
@Service
public class TurkeyAddressService {

    public record Place(String name, String postalCode, boolean village) {
        /** The text written at the start of AddressLine, for example "Ozluce Mah." or "Akdurak Koyu". */
        public String label() {
            return name + (village ? " Köyü" : " Mah.");
        }
    }

    public record District(String name, List<Place> places) {
    }

    public record Province(int id, String name, List<District> districts) {
    }

    /** A checked address with the official names and the postal code of the chosen place. */
    public record Match(Province province, District district, Place place) {
    }

    private final List<Province> provinces;

    public TurkeyAddressService(ObjectMapper objectMapper) {
        List<Province> loaded = new ArrayList<>();
        try {
            JsonNode list = read(objectMapper, "static/data/tr/provinces.json");
            for (JsonNode p : list) {
                JsonNode data = read(objectMapper, "static/data/tr/p" + p.get("id").asInt() + ".json");
                List<District> districts = new ArrayList<>();
                for (JsonNode d : data.get("districts")) {
                    List<Place> places = new ArrayList<>();
                    for (JsonNode x : d.get("places")) {
                        places.add(new Place(x.get(0).asText(), x.get(1).asText(), x.get(2).asInt() == 1));
                    }
                    districts.add(new District(d.get("name").asText(), List.copyOf(places)));
                }
                loaded.add(new Province(p.get("id").asInt(), p.get("name").asText(), List.copyOf(districts)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Turkish address data could not be read", e);
        }
        this.provinces = List.copyOf(loaded);
    }

    private static JsonNode read(ObjectMapper mapper, String path) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return mapper.readTree(in);
        }
    }

    public List<Province> provinces() {
        return provinces;
    }

    public Optional<Province> province(String name) {
        String key = key(name);
        return provinces.stream().filter(p -> key(p.name()).equals(key)).findFirst();
    }

    public List<District> districts(String provinceName) {
        return province(provinceName).map(Province::districts).orElse(Collections.emptyList());
    }

    public Optional<District> district(String provinceName, String districtName) {
        String key = key(districtName);
        return districts(provinceName).stream().filter(d -> key(d.name()).equals(key)).findFirst();
    }

    public List<Place> places(String provinceName, String districtName) {
        return district(provinceName, districtName).map(District::places).orElse(Collections.emptyList());
    }

    /** Finds a place by its label ("Ozluce Mah.") or by its name alone. */
    public Optional<Place> place(String provinceName, String districtName, String placeLabel) {
        String key = key(placeLabel);
        return places(provinceName, districtName).stream()
                .filter(p -> key(p.label()).equals(key) || key(p.name()).equals(key))
                .findFirst();
    }

    public Optional<Match> match(String provinceName, String districtName, String placeLabel) {
        Optional<Province> province = province(provinceName);
        Optional<District> district = district(provinceName, districtName);
        Optional<Place> place = place(provinceName, districtName, placeLabel);
        if (province.isEmpty() || district.isEmpty() || place.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Match(province.get(), district.get(), place.get()));
    }

    /**
     * Splits a saved AddressLine such as "Ozluce Mah., 15. Sk. No:9" into the place and the rest.
     * Returns empty when the line does not start with a place of that district (older addresses).
     */
    public Optional<String[]> splitLine(String provinceName, String districtName, String addressLine) {
        if (addressLine == null) {
            return Optional.empty();
        }
        int comma = addressLine.indexOf(',');
        if (comma <= 0) {
            return Optional.empty();
        }
        String head = addressLine.substring(0, comma).trim();
        String rest = addressLine.substring(comma + 1).trim();
        return place(provinceName, districtName, head).map(p -> new String[] {p.label(), rest});
    }

    /** "Şişli" becomes "Sisli": the form the VARCHAR columns keep. */
    public static String plain(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            out.append(switch (c) {
                case 'ç' -> 'c'; case 'Ç' -> 'C'; case 'ğ' -> 'g'; case 'Ğ' -> 'G';
                case 'ı' -> 'i'; case 'İ' -> 'I'; case 'ö' -> 'o'; case 'Ö' -> 'O';
                case 'ş' -> 's'; case 'Ş' -> 'S'; case 'ü' -> 'u'; case 'Ü' -> 'U';
                case 'â' -> 'a'; case 'Â' -> 'A'; case 'î' -> 'i'; case 'Î' -> 'I';
                case 'û' -> 'u'; case 'Û' -> 'U';
                default -> c;
            });
        }
        return out.toString();
    }

    /** Comparison key: no Turkish letters, no case, single spaces. */
    public static String key(String text) {
        if (text == null) {
            return "";
        }
        return plain(text).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
