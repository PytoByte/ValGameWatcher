package pytobyte.valcompchecker.data

enum class Gamemodes(val type: String, val translate: String) {
    COMPETITIVE("competitive", "Рейтинговая игра"),
    PREMIER("premier", "Премьер"),
    UNRATED("unrated", "Без ранга"),
    TEAMDEATHMATCH("team-deathmatch", "Командный ДМ"),
    DEATHMATCH("deathmatch", "ДМ"),
    SPIKERUSH("spikerush", "Быстр. установка спайка"),
    SWIFTPLAY("swiftplay", "Быстр. игра"),
    ESCALATION("escalation", "Эскалация"),
    REPLICATION("replication", "Репликация"),
    SNOWBALL("snowball", "Снежки"),
    NEWMAPBOMB("newmap-bomb", "Новая карта без ранга"),
    NEWMAPSWIFTPLAY("newmap-swiftplay", "Новая карта быстр. игра");
}