package com.pokedex;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.stream.Collectors;


public class Pokemon {

    private int id;
    private String name;
    private int height;
    private int weight;
    private List<TypeSlot> types;
    private List<AbilitySlot> abilities;
    private Sprites sprites;

    public int getId() { return id; }
    public String getName() { return name; }
    public double getHeightInMeters() { return height / 10.0; }
    public double getWeightInKg() { return weight / 10.0; }

    public String getTypesAsText() {
        return types.stream().map(t -> t.type.name).collect(Collectors.joining(", "));
    }

    public String getAbilitiesAsText() {
        return abilities.stream().map(a -> a.ability.name).collect(Collectors.joining(", "));
    }

    public String getSpriteUrl() {
        return sprites != null ? sprites.frontDefault : null;
    }

    @Override
    public String toString() {
        return String.format(
            "#%d - %s%n  Tipo(s): %s%n  Altura: %.1f m%n  Peso: %.1f kg%n  Habilidades: %s%n  Imagem: %s",
            id, capitalize(name), getTypesAsText(), getHeightInMeters(),
            getWeightInKg(), getAbilitiesAsText(), getSpriteUrl());
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }


    static class TypeSlot { NamedResource type; }
    static class AbilitySlot { NamedResource ability; }
    static class NamedResource { String name; }
    static class Sprites {
        @SerializedName("front_default")
        String frontDefault;
    }
}
