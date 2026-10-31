package com.netheryx.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netheryx.NetheryX;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Animations d'origine de la version 1.20.1 (fichiers {@code animations/<creature>.animation.json}, format GeckoLib /
 * Bedrock) : images cles de rotation, position et echelle par os, avec leurs courbes d'acceleration. Elles sont
 * rejouees telles quelles par {@link GeoModel}.
 */
public final class GeoAnimation {
    /** Une image cle : instant (secondes), valeur (x, y, z) et courbe pour y arriver depuis l'image precedente. */
    static final class Keyframe {
        final float time;
        final float[] pre;
        final float[] post;
        final String easing;

        Keyframe(float time, float[] pre, float[] post, String easing) {
            this.time = time;
            this.pre = pre;
            this.post = post;
            this.easing = easing;
        }
    }

    /** Pistes d'un os : rotation, position, echelle (null si absente). */
    static final class BoneTrack {
        List<Keyframe> rotation;
        List<Keyframe> position;
        List<Keyframe> scale;
    }

    final String name;
    final float length;
    final boolean loop;
    final Map<String, BoneTrack> bones = new HashMap<String, BoneTrack>();

    private GeoAnimation(String name, float length, boolean loop) {
        this.name = name;
        this.length = length;
        this.loop = loop;
    }

    /** Animations d'une creature, par nom court (idle, walk, fly, attack...). Vide si le fichier manque. */
    public static Map<String, GeoAnimation> load(String creature) {
        Map<String, GeoAnimation> result = new HashMap<String, GeoAnimation>();
        ResourceLocation location = new ResourceLocation(NetheryX.MOD_ID, "animations/" + creature + ".animation.json");
        try {
            InputStreamReader reader = new InputStreamReader(
                    Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream(), "UTF-8");
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();
            JsonObject animations = root.getAsJsonObject("animations");
            for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
                String key = entry.getKey();
                String shortName = key.substring(key.lastIndexOf('.') + 1);
                JsonObject animation = entry.getValue().getAsJsonObject();
                JsonElement loopElement = animation.get("loop");
                boolean loop = loopElement != null && loopElement.isJsonPrimitive()
                        && (loopElement.getAsJsonPrimitive().isBoolean() ? loopElement.getAsBoolean() : "loop".equals(loopElement.getAsString()));
                float length = animation.has("animation_length") ? animation.get("animation_length").getAsFloat() : 1.0F;
                GeoAnimation parsed = new GeoAnimation(shortName, length, loop);
                if (animation.has("bones")) {
                    for (Map.Entry<String, JsonElement> bone : animation.getAsJsonObject("bones").entrySet()) {
                        JsonObject channels = bone.getValue().getAsJsonObject();
                        BoneTrack track = new BoneTrack();
                        track.rotation = channel(channels.get("rotation"));
                        track.position = channel(channels.get("position"));
                        track.scale = channel(channels.get("scale"));
                        parsed.bones.put(bone.getKey(), track);
                    }
                }
                result.put(shortName, parsed);
            }
        } catch (Exception e) {
            NetheryX.LOG.debug("Pas d'animation pour " + creature + " : " + e);
        }
        return result;
    }

    private static float[] vector(JsonElement element) {
        if (element == null) {
            return null;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
        }
        if (element.isJsonPrimitive()) {
            float value = element.getAsFloat();
            return new float[]{value, value, value};
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("vector")) {
            return vector(object.get("vector"));
        }
        if (object.has("post")) {
            return vector(object.get("post"));
        }
        return vector(object.get("pre"));
    }

    private static List<Keyframe> channel(JsonElement element) {
        if (element == null) {
            return null;
        }
        List<Keyframe> frames = new ArrayList<Keyframe>();
        if (element.isJsonArray() || element.isJsonPrimitive()) {
            float[] value = vector(element);
            frames.add(new Keyframe(0.0F, value, value, "linear"));
            return frames;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("vector")) {
            float[] value = vector(object);
            frames.add(new Keyframe(0.0F, value, value, "linear"));
            return frames;
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            float time;
            try {
                time = Float.parseFloat(entry.getKey());
            } catch (NumberFormatException e) {
                continue;
            }
            JsonElement value = entry.getValue();
            float[] pre = vector(value);
            float[] post = pre;
            String easing = "linear";
            if (value.isJsonObject()) {
                JsonObject frame = value.getAsJsonObject();
                if (frame.has("pre")) {
                    pre = vector(frame.get("pre"));
                }
                if (frame.has("post")) {
                    post = vector(frame.get("post"));
                }
                if (frame.has("easing")) {
                    easing = frame.get("easing").getAsString();
                } else if (frame.has("lerp_mode")) {
                    easing = "catmullrom".equals(frame.get("lerp_mode").getAsString()) ? "easeInOutSine" : "linear";
                }
            }
            if (pre != null && post != null) {
                frames.add(new Keyframe(time, pre, post, easing));
            }
        }
        Collections.sort(frames, new Comparator<Keyframe>() {
            @Override
            public int compare(Keyframe a, Keyframe b) {
                return Float.compare(a.time, b.time);
            }
        });
        return frames.isEmpty() ? null : frames;
    }

    /** Valeur d'une piste a l'instant t (secondes), ou null si la piste est absente. */
    static float[] sample(List<Keyframe> frames, float t) {
        if (frames == null) {
            return null;
        }
        Keyframe first = frames.get(0);
        if (t <= first.time || frames.size() == 1) {
            return first.pre;
        }
        for (int i = 1; i < frames.size(); i++) {
            Keyframe next = frames.get(i);
            if (t <= next.time) {
                Keyframe previous = frames.get(i - 1);
                float span = next.time - previous.time;
                float k = span <= 0.0F ? 1.0F : (t - previous.time) / span;
                // Comme GeckoLib : la courbe est celle de l'image cle d'arrivee
                float e = ease(next.easing, k);
                return new float[]{lerp(previous.post[0], next.pre[0], e), lerp(previous.post[1], next.pre[1], e),
                        lerp(previous.post[2], next.pre[2], e)};
            }
        }
        return frames.get(frames.size() - 1).post;
    }

    private static float lerp(float a, float b, float k) {
        return a + (b - a) * k;
    }

    /** Courbes d'acceleration (memes formules que GeckoLib / easings.net). */
    static float ease(String name, float x) {
        double t = x;
        double c1 = 1.70158D;
        double c3 = c1 + 1.0D;
        double c4 = 2.0D * Math.PI / 3.0D;
        double r;
        if ("easeInSine".equals(name)) {
            r = 1.0D - Math.cos(t * Math.PI / 2.0D);
        } else if ("easeOutSine".equals(name)) {
            r = Math.sin(t * Math.PI / 2.0D);
        } else if ("easeInOutSine".equals(name)) {
            r = -(Math.cos(Math.PI * t) - 1.0D) / 2.0D;
        } else if ("easeInQuad".equals(name)) {
            r = t * t;
        } else if ("easeOutQuad".equals(name)) {
            r = 1.0D - (1.0D - t) * (1.0D - t);
        } else if ("easeInOutQuad".equals(name)) {
            r = t < 0.5D ? 2.0D * t * t : 1.0D - Math.pow(-2.0D * t + 2.0D, 2.0D) / 2.0D;
        } else if ("easeInCubic".equals(name)) {
            r = t * t * t;
        } else if ("easeOutCubic".equals(name)) {
            r = 1.0D - Math.pow(1.0D - t, 3.0D);
        } else if ("easeInOutCubic".equals(name)) {
            r = t < 0.5D ? 4.0D * t * t * t : 1.0D - Math.pow(-2.0D * t + 2.0D, 3.0D) / 2.0D;
        } else if ("easeInExpo".equals(name)) {
            r = t == 0.0D ? 0.0D : Math.pow(2.0D, 10.0D * t - 10.0D);
        } else if ("easeOutExpo".equals(name)) {
            r = t == 1.0D ? 1.0D : 1.0D - Math.pow(2.0D, -10.0D * t);
        } else if ("easeInOutExpo".equals(name)) {
            r = t == 0.0D ? 0.0D : t == 1.0D ? 1.0D : t < 0.5D ? Math.pow(2.0D, 20.0D * t - 10.0D) / 2.0D
                    : (2.0D - Math.pow(2.0D, -20.0D * t + 10.0D)) / 2.0D;
        } else if ("easeInBack".equals(name)) {
            r = c3 * t * t * t - c1 * t * t;
        } else if ("easeOutBack".equals(name)) {
            r = 1.0D + c3 * Math.pow(t - 1.0D, 3.0D) + c1 * Math.pow(t - 1.0D, 2.0D);
        } else if ("easeInOutBack".equals(name)) {
            double c2 = c1 * 1.525D;
            r = t < 0.5D ? Math.pow(2.0D * t, 2.0D) * ((c2 + 1.0D) * 2.0D * t - c2) / 2.0D
                    : (Math.pow(2.0D * t - 2.0D, 2.0D) * ((c2 + 1.0D) * (t * 2.0D - 2.0D) + c2) + 2.0D) / 2.0D;
        } else if ("easeOutBounce".equals(name)) {
            r = bounce(t);
        } else if ("easeInBounce".equals(name)) {
            r = 1.0D - bounce(1.0D - t);
        } else if ("easeOutElastic".equals(name)) {
            r = t == 0.0D ? 0.0D : t == 1.0D ? 1.0D : Math.pow(2.0D, -10.0D * t) * Math.sin((t * 10.0D - 0.75D) * c4) + 1.0D;
        } else if ("easeInElastic".equals(name)) {
            r = t == 0.0D ? 0.0D : t == 1.0D ? 1.0D : -Math.pow(2.0D, 10.0D * t - 10.0D) * Math.sin((t * 10.0D - 10.75D) * c4);
        } else if ("step".equals(name)) {
            r = t < 1.0D ? 0.0D : 1.0D;
        } else {
            r = t;
        }
        return (float) r;
    }

    private static double bounce(double t) {
        double n1 = 7.5625D;
        double d1 = 2.75D;
        if (t < 1.0D / d1) {
            return n1 * t * t;
        } else if (t < 2.0D / d1) {
            t -= 1.5D / d1;
            return n1 * t * t + 0.75D;
        } else if (t < 2.5D / d1) {
            t -= 2.25D / d1;
            return n1 * t * t + 0.9375D;
        }
        t -= 2.625D / d1;
        return n1 * t * t + 0.984375D;
    }
}
