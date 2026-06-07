package ru.purpir.eventaltar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventAltarSavedData extends PersistentState {
    private static final String NAME = "caveborn_event_altar_global";
    public static final int MAX_LEVEL = 10;
    public static final int QUEST_COUNT = 5;

    private int altarLevel = 1;
    private int altarXp = 0;
    private int totalCompleted = 0;
    private long boardHour = -1L;
    private List<QuestState> quests = new ArrayList<>();

    public record QuestState(
        int id,
        int type,
        int rarity,
        int target,
        int progress,
        String claimedBy,
        boolean rewardReady,
        boolean completed
    ) {
        public static final Codec<QuestState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.INT.fieldOf("id").forGetter(QuestState::id),
                Codec.INT.fieldOf("type").forGetter(QuestState::type),
                Codec.INT.fieldOf("rarity").forGetter(QuestState::rarity),
                Codec.INT.fieldOf("target").forGetter(QuestState::target),
                Codec.INT.fieldOf("progress").forGetter(QuestState::progress),
                Codec.STRING.fieldOf("claimed_by").forGetter(QuestState::claimedBy),
                Codec.BOOL.fieldOf("reward_ready").forGetter(QuestState::rewardReady),
                Codec.BOOL.fieldOf("completed").forGetter(QuestState::completed)
            ).apply(instance, QuestState::new)
        );

        public boolean isClaimed() {
            return !claimedBy.isEmpty();
        }

        public boolean isClaimedBy(UUID uuid) {
            return claimedBy.equals(uuid.toString());
        }

        public QuestState claim(UUID uuid) {
            return new QuestState(id, type, rarity, target, progress, uuid.toString(), rewardReady, completed);
        }

        public QuestState cancel() {
            return new QuestState(id, type, rarity, target, progress, "", false, completed);
        }

        public QuestState advance(int amount) {
            int updated = Math.min(target, progress + amount);
            return new QuestState(id, type, rarity, target, updated, claimedBy, updated >= target, completed);
        }

        public QuestState finish() {
            return new QuestState(id, type, rarity, target, target, claimedBy, false, true);
        }
    }

    public static final Codec<EventAltarSavedData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("altar_level").forGetter(data -> data.altarLevel),
            Codec.INT.fieldOf("altar_xp").forGetter(data -> data.altarXp),
            Codec.INT.fieldOf("total_completed").forGetter(data -> data.totalCompleted),
            Codec.LONG.fieldOf("board_hour").forGetter(data -> data.boardHour),
            QuestState.CODEC.listOf().fieldOf("quests").forGetter(data -> data.quests)
        ).apply(instance, EventAltarSavedData::new)
    );

    public static final PersistentStateType<EventAltarSavedData> TYPE = new PersistentStateType<>(
        NAME,
        context -> new EventAltarSavedData(),
        context -> CODEC,
        null
    );

    public EventAltarSavedData() {
    }

    public EventAltarSavedData(int altarLevel, int altarXp, int totalCompleted, long boardHour, List<QuestState> quests) {
        this.altarLevel = altarLevel;
        this.altarXp = altarXp;
        this.totalCompleted = totalCompleted;
        this.boardHour = boardHour;
        this.quests = new ArrayList<>(quests);
    }

    public static EventAltarSavedData get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        EventAltarSavedData data = overworld.getPersistentStateManager().getOrCreate(TYPE);
        data.refreshBoardIfNeeded();
        return data;
    }

    public int getAltarLevel() {
        return altarLevel;
    }

    public int getAltarXp() {
        return altarXp;
    }

    public int getTotalCompleted() {
        return totalCompleted;
    }

    public int getXpForNextLevel() {
        return Math.min(MAX_LEVEL, altarLevel) * 100;
    }

    public List<QuestState> getQuests() {
        return quests;
    }

    public QuestState getQuest(int id) {
        for (QuestState quest : quests) {
            if (quest.id() == id) {
                return quest;
            }
        }
        return null;
    }

    public void replaceQuest(QuestState updated) {
        for (int i = 0; i < quests.size(); i++) {
            if (quests.get(i).id() == updated.id()) {
                quests.set(i, updated);
                markDirty();
                return;
            }
        }
    }

    public void addAltarXp(int amount) {
        if (altarLevel >= MAX_LEVEL) {
            altarXp = 0;
            markDirty();
            return;
        }

        altarXp += amount;
        while (altarLevel < MAX_LEVEL && altarXp >= getXpForNextLevel()) {
            altarXp -= getXpForNextLevel();
            altarLevel++;
        }
        if (altarLevel >= MAX_LEVEL) {
            altarXp = 0;
        }
        markDirty();
    }

    public void completeQuest(QuestState quest, int xp) {
        replaceQuest(quest.finish());
        totalCompleted++;
        addAltarXp(xp);
        markDirty();
    }

    public void refreshBoardIfNeeded() {
        long currentHour = System.currentTimeMillis() / 3_600_000L;
        if (boardHour == currentHour && quests.size() == QUEST_COUNT) {
            return;
        }

        boardHour = currentHour;
        quests = EventAltarQuestPool.generate(currentHour, altarLevel);
        markDirty();
    }
}
