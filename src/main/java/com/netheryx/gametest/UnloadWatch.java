package com.netheryx.gametest;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Espion pose sur la liste des chunks a decharger de Minecraft. Elle ne doit jamais changer pendant son parcours : sinon
 * le jeu plante (ArrayIndexOutOfBoundsException dans LongOpenHashSet). Cela arrive quand du code lance pendant le
 * dechargement d'une entite charge un chunk. L'espion garde la pile d'appels fautive.
 */
final class UnloadWatch extends LongOpenHashSet {
    final List<String> violations = new ArrayList<>();
    int unloaded;
    private boolean iterating;

    private UnloadWatch() {
    }

    static UnloadWatch install(ServerLevel level) throws ReflectiveOperationException {
        Field managerField = find(ServerLevel.class, PersistentEntitySectionManager.class);
        Object manager = managerField.get(level);
        Field setField = find(PersistentEntitySectionManager.class, it.unimi.dsi.fastutil.longs.LongSet.class, "chunksToUnload");
        UnloadWatch watch = new UnloadWatch();
        watch.addAll((it.unimi.dsi.fastutil.longs.LongSet) setField.get(manager));
        setField.set(manager, watch);
        return watch;
    }

    private static Field find(Class<?> owner, Class<?> type, String... preferred) throws NoSuchFieldException {
        Field match = null;
        for (Field field : owner.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType()) && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                if (match == null || (preferred.length > 0 && field.getName().equals(preferred[0]))) {
                    match = field;
                }
            }
        }
        if (match == null) {
            throw new NoSuchFieldException(type.getName());
        }
        return match;
    }

    private void check() {
        if (this.iterating && this.violations.size() < 3) {
            StringBuilder trace = new StringBuilder();
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                trace.append("\n    ").append(element);
            }
            this.violations.add(trace.toString());
        }
    }

    @Override
    public boolean add(long key) {
        this.check();
        return super.add(key);
    }

    @Override
    public boolean remove(long key) {
        if (!this.iterating) {
            return super.remove(key);
        }
        this.check();
        return super.remove(key);
    }

    @Override
    public boolean removeIf(java.util.function.LongPredicate filter) {
        boolean outer = this.iterating;
        this.iterating = true;
        try {
            // Parcours sur une copie : l'espion doit tout voir sans planter lui-meme
            long[] keys = this.toLongArray();
            boolean changed = false;
            for (long key : keys) {
                if (filter.test(key)) {
                    this.iterating = false;
                    super.remove(key);
                    this.iterating = true;
                    this.unloaded++;
                    changed = true;
                }
            }
            return changed;
        } finally {
            this.iterating = outer;
        }
    }
}
