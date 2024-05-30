package bot.permissionsystem;

public enum BotPermission {
    MUSIC_NORMAL("Grundlegende Sprach-Berechtigungen (z.B. Im Sprachkanal reden)"),
    MUSIC_ADVANCED("Weiterführende Sprach-Berechtigungen (z.B. Mitglieder in Sprachkanal verschieben)"),
    TEXT_NORMAL("Grundlegende Text-Berechtigungen (z.B. Nachrichten senden)"),
    TEXT_ADVANCED("Weiterführende Text-Berechtigungen (z.B. Nachrichten verwalten)"),
    ADMIN("Administrator-Berechtigungen");

    private final String description;

    BotPermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMusicPermission() {
        return this == MUSIC_NORMAL || this == MUSIC_ADVANCED;
    }

    public boolean isTextPermission() {
        return this == TEXT_NORMAL || this == TEXT_ADVANCED;
    }
}