package bot.permissionsystem;

public enum BotPermission {
    VOICE_NORMAL("Grundlegende Sprach-Berechtigungen (z.B. Im Sprachkanal reden)"),
    VOICE_ADVANCED("Weiterführende Sprach-Berechtigungen (z.B. Mitglieder in Sprachkanal verschieben)"),
    TEXT_NORMAL("Grundlegende Text-Berechtigungen (z.B. Nachrichten senden)"),
    TEXT_ADVANCED("Weiterführende Text-Berechtigungen (z.B. Nachrichten verwalten)"),
    ADMIN("Administrator-Berechtigungen"),

    BOT_VOICE(""),
    BOT_TEXT(""),
    BOT_ADMIN("");

    private String description;

    BotPermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public boolean isBotBotPermission() {
        return this == BOT_VOICE || this == BOT_TEXT || this == BOT_ADMIN;
    }
}