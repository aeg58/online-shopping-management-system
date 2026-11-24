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

@Service
public class TurkeyAddressService {

    public record Place(String name, String postalCode, boolean village) {
        public String label() {
            return name + (village ? " Köyü" : " Mah.");
        }
    }

    public record District(String name, List<Place> places) {
    }

    public record Province(int id, String name, List<District> districts) {
    }

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

    public static String key(String text) {
        if (text == null) {
            return "";
        }
        return plain(text).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
