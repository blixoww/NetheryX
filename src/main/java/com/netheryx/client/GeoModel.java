package com.netheryx.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netheryx.NetheryX;
import com.netheryx.entity.IAnimatedMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modele de creature construit a partir des fichiers de geometrie (format Bedrock) de la version 1.20.1 : les memes
 * os, cubes et textures, convertis en ModelRenderer de la 1.7.10 (axe X retourne, Y vers le bas, origine a 24).
 * L'animation est procedurale : pattes, bras, ailes, queues, tete et pieces speciales sont reconnus a leur nom.
 */
public class GeoModel extends ModelBase {
    private static final float DEG = (float) Math.PI / 180.0F;

    private final List<ModelRenderer> roots = new ArrayList<ModelRenderer>();
    private final List<Bone> bones = new ArrayList<Bone>();

    private static final class Bone {
        final String name;
        final ModelRenderer part;
        final float rx;
        final float ry;
        final float rz;
        final boolean left;
        final boolean back;
        final int index;

        Bone(String name, ModelRenderer part, int index) {
            this.name = name;
            this.part = part;
            this.rx = part.rotateAngleX;
            this.ry = part.rotateAngleY;
            this.rz = part.rotateAngleZ;
            this.left = name.contains("left") || name.matches(".*_l\\d.*");
            this.back = name.contains("back");
            this.index = index;
        }
    }

    public GeoModel(String name) {
        ResourceLocation location = new ResourceLocation(NetheryX.MOD_ID, "geo/" + name + ".geo.json");
        try {
            InputStreamReader reader = new InputStreamReader(
                    Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream(), "UTF-8");
            JsonObject geometry = new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("minecraft:geometry")
                    .get(0).getAsJsonObject();
            reader.close();
            JsonObject description = geometry.getAsJsonObject("description");
            this.textureWidth = description.get("texture_width").getAsInt();
            this.textureHeight = description.get("texture_height").getAsInt();
            build(geometry.getAsJsonArray("bones"));
        } catch (Exception e) {
            NetheryX.LOG.error("Modele introuvable ou illisible : " + location, e);
        }
    }

    private static float[] vec(JsonObject object, String key) {
        JsonArray array = object.has(key) ? object.getAsJsonArray(key) : null;
        if (array == null) {
            return new float[]{0, 0, 0};
        }
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    /** Point de rotation absolu en coordonnees 1.7.10. */
    private static float[] java(float[] pivot) {
        return new float[]{-pivot[0], 24.0F - pivot[1], pivot[2]};
    }

    private void build(JsonArray array) {
        Map<String, ModelRenderer> parts = new HashMap<String, ModelRenderer>();
        Map<String, float[]> pivots = new HashMap<String, float[]>();
        int index = 0;
        for (JsonElement element : array) {
            JsonObject bone = element.getAsJsonObject();
            String name = bone.get("name").getAsString();
            float[] pivot = java(vec(bone, "pivot"));
            ModelRenderer part = new ModelRenderer(this, name);
            part.setTextureSize(this.textureWidth, this.textureHeight);
            part.mirror = true;
            String parent = bone.has("parent") ? bone.get("parent").getAsString() : null;
            float[] parentPivot = parent != null && pivots.containsKey(parent) ? pivots.get(parent) : new float[]{0, 0, 0};
            part.setRotationPoint(pivot[0] - parentPivot[0], pivot[1] - parentPivot[1], pivot[2] - parentPivot[2]);
            float[] rotation = vec(bone, "rotation");
            part.rotateAngleX = rotation[0] * DEG;
            part.rotateAngleY = -rotation[1] * DEG;
            part.rotateAngleZ = -rotation[2] * DEG;
            if (bone.has("cubes")) {
                for (JsonElement cubeElement : bone.getAsJsonArray("cubes")) {
                    addCube(part, pivot, cubeElement.getAsJsonObject());
                }
            }
            if (parent != null && parts.containsKey(parent)) {
                parts.get(parent).addChild(part);
            } else {
                this.roots.add(part);
            }
            parts.put(name, part);
            pivots.put(name, pivot);
            this.bones.add(new Bone(name, part, index++));
        }
    }

    private void addCube(ModelRenderer part, float[] bonePivot, JsonObject cube) {
        float[] origin = vec(cube, "origin");
        float[] size = vec(cube, "size");
        JsonArray uv = cube.getAsJsonArray("uv");
        float inflate = cube.has("inflate") ? cube.get("inflate").getAsFloat() : 0.0F;
        int w = Math.max(1, MathHelper.ceiling_float_int(size[0]));
        int h = Math.max(1, MathHelper.ceiling_float_int(size[1]));
        int d = Math.max(1, MathHelper.ceiling_float_int(size[2]));
        // Les tailles fractionnaires sont arrondies au pixel superieur, centrees sur le cube d'origine
        float ox = origin[0] - (w - size[0]) / 2.0F;
        float oy = origin[1] - (h - size[1]) / 2.0F;
        float oz = origin[2] - (d - size[2]) / 2.0F;
        float x0 = -(ox + w);
        float y0 = 24.0F - (oy + h);
        float z0 = oz;
        int u = uv.get(0).getAsInt();
        int v = uv.get(1).getAsInt();
        if (cube.has("rotation")) {
            float[] cubePivot = java(cube.has("pivot") ? vec(cube, "pivot") : origin);
            float[] rotation = vec(cube, "rotation");
            ModelRenderer holder = new ModelRenderer(this);
            holder.setTextureSize(this.textureWidth, this.textureHeight);
            holder.mirror = true;
            holder.setRotationPoint(cubePivot[0] - bonePivot[0], cubePivot[1] - bonePivot[1], cubePivot[2] - bonePivot[2]);
            holder.rotateAngleX = rotation[0] * DEG;
            holder.rotateAngleY = -rotation[1] * DEG;
            holder.rotateAngleZ = -rotation[2] * DEG;
            holder.setTextureOffset(u, v).addBox(x0 - cubePivot[0], y0 - cubePivot[1], z0 - cubePivot[2], w, h, d, inflate);
            part.addChild(holder);
        } else {
            part.setTextureOffset(u, v).addBox(x0 - bonePivot[0], y0 - bonePivot[1], z0 - bonePivot[2], w, h, d, inflate);
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbAmount, float age, float yaw, float pitch, float scale) {
        setRotationAngles(limbSwing, limbAmount, age, yaw, pitch, scale, entity);
        for (ModelRenderer root : this.roots) {
            root.render(scale);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbAmount, float age, float yaw, float pitch, float scale, Entity entity) {
        boolean flying = entity instanceof IAnimatedMob && ((IAnimatedMob) entity).isFlyingAnimation();
        float action = entity instanceof IAnimatedMob ? ((IAnimatedMob) entity).getActionProgress() : 0.0F;
        boolean headDone = false;
        for (Bone bone : this.bones) {
            ModelRenderer p = bone.part;
            p.rotateAngleX = bone.rx;
            p.rotateAngleY = bone.ry;
            p.rotateAngleZ = bone.rz;
            String n = bone.name;
            float side = bone.left ? 1.0F : -1.0F;
            if ((n.equals("head") || n.equals("neck1")) && !headDone) {
                p.rotateAngleY += yaw * DEG * 0.8F;
                p.rotateAngleX += pitch * DEG * 0.8F;
                headDone = true;
            } else if (n.contains("leg") || n.startsWith("thigh")) {
                float phase = (bone.left ? 0.0F : (float) Math.PI) + (bone.back || bone.index % 2 == 1 && n.matches(".*\\d.*") ? (float) Math.PI : 0.0F);
                p.rotateAngleX += flying ? 0.6F : MathHelper.cos(limbSwing * 0.6662F + phase) * 1.1F * limbAmount;
            } else if (n.startsWith("arm") || n.startsWith("claw")) {
                p.rotateAngleX += MathHelper.cos(limbSwing * 0.6662F + (bone.left ? (float) Math.PI : 0.0F)) * 0.8F * limbAmount
                        - action * 1.6F;
                p.rotateAngleZ += side * MathHelper.sin(age * 0.067F) * 0.05F;
            } else if (n.startsWith("wing") || n.startsWith("elytron")) {
                float speed = flying ? 0.6F : 0.12F;
                float amplitude = flying ? 0.7F : 0.15F;
                if (n.endsWith("_mid") || n.endsWith("_tip")) {
                    amplitude *= 0.5F;
                }
                p.rotateAngleZ += side * MathHelper.sin(age * speed) * amplitude;
            } else if (n.startsWith("tail") || n.startsWith("segment")) {
                p.rotateAngleY += MathHelper.sin(age * 0.15F + bone.index) * 0.2F + MathHelper.cos(limbSwing * 0.6662F) * 0.3F * limbAmount;
            } else if (n.equals("jaw")) {
                p.rotateAngleX += action * 0.7F;
            } else if (n.equals("lid")) {
                p.rotateAngleX -= action * 0.9F;
            } else if (n.equals("orbit") || n.startsWith("crystal")) {
                p.rotateAngleY += age * 0.05F;
            } else if (n.startsWith("antenna")) {
                p.rotateAngleX += MathHelper.sin(age * 0.2F + bone.index) * 0.15F;
            }
        }
    }
}
