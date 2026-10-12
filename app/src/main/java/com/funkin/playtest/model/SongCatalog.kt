package com.funkin.playtest.model

object SongCatalog {
    val songs = listOf(
        SongData("tutorial", "Tutorial", "Tutorial", 100f, "Girlfriend", "Boyfriend", "Stage", "Learn the basics of Friday Night Funkin'!"),
        SongData("bopeebo", "Bopeebo", "Week 1", 100f, "Daddy Dearest", "Boyfriend", "Stage", "Groovy warm-up against Daddy Dearest."),
        SongData("fresh", "Fresh", "Week 1", 120f, "Daddy Dearest", "Boyfriend", "Stage", "Bouncy syncopated rhythms with classic beat drops."),
        SongData("dadbattle", "Dad Battle", "Week 1", 180f, "Daddy Dearest", "Boyfriend", "Stage", "High tempo musical duel for GF's approval!"),
        SongData("spookeez", "Spookeez", "Week 2", 150f, "Skid and Pump", "Boyfriend", "Spooky Mansion", "Spooky Month dancing duo rhythm clash."),
        SongData("south", "South", "Week 2", 165f, "Skid and Pump", "Boyfriend", "Spooky Mansion", "Hard-hitting bass and rapid double notes."),
        SongData("monster", "Monster", "Week 2", 95f, "Monster", "Boyfriend", "Spooky Mansion", "Eerie, methodical and spine-chilling flow."),
        SongData("pico", "Pico", "Week 3", 150f, "Pico", "Boyfriend", "Newgrounds City", "Classic Newgrounds hero armed with a mic."),
        SongData("philly", "Philly Nice", "Week 3", 175f, "Pico", "Boyfriend", "Subway Station", "Speedy Philly train tracks beat frenzy."),
        SongData("blammed", "Blammed", "Week 3", 165f, "Pico", "Boyfriend", "Subway Station", "Intense multi-directional stream bursts."),
        SongData("satin-panties", "Satin Panties", "Week 4", 110f, "Mommy Mearest", "Boyfriend", "Limousine", "Smooth synthwave ride atop a speeding limo."),
        SongData("high", "High", "Week 4", 125f, "Mommy Mearest", "Boyfriend", "Limousine", "Euphoric dance pop rhythm patterns."),
        SongData("milf", "M.I.L.F", "Week 4", 180f, "Mommy Mearest", "Boyfriend", "Limousine", "Legendary hard drop beat section!"),
        SongData("senpai", "Senpai", "Week 6", 144f, "Senpai", "Boyfriend", "School Sim", "Chiptune dating sim arcade rhythm vibes."),
        SongData("ugh", "Ugh", "Week 7", 160f, "Tankman", "Boyfriend", "Military Desert", "Military base rap battle shouting 'UGH!'"),
        SongData("guns", "Guns", "Week 7", 185f, "Tankman", "Boyfriend", "Military Desert", "Adrenaline pumping Tankman solo run.")
    )

    val weeks = listOf(
        WeekData("week0", "How To Play", "Tutorial", "Girlfriend", listOf(songs[0])),
        WeekData("week1", "Daddy Dearest", "Week 1", "Daddy Dearest", listOf(songs[1], songs[2], songs[3])),
        WeekData("week2", "Spooky Month", "Week 2", "Skid and Pump", listOf(songs[4], songs[5], songs[6])),
        WeekData("week3", "Pico", "Week 3", "Pico", listOf(songs[7], songs[8], songs[9])),
        WeekData("week4", "Mommy Must Murder", "Week 4", "Mommy Mearest", listOf(songs[10], songs[11], songs[12])),
        WeekData("week6", "Hating Simulator", "Week 6", "Senpai", listOf(songs[13])),
        WeekData("week7", "Tankman", "Week 7", "Tankman", listOf(songs[14], songs[15]))
    )

    fun getSong(id: String): SongData = songs.firstOrNull { it.id == id } ?: songs[0]

    /**
     * Generates a realistic FNF chart with call-and-response structure:
     * Opponent sings first, then Player echoes or responds with variations!
     */
    fun generateChart(song: SongData, difficulty: Difficulty): List<GameNote> {
        val notes = mutableListOf<GameNote>()
        val bpm = song.bpm
        val stepMs = (60_000f / bpm) / 4f // 16th note step in ms
        val totalMeasures = 32 // around 1:30 - 2:00 duration
        var noteId = 1L

        val density = when (difficulty) {
            Difficulty.EASY -> 0.45f
            Difficulty.NORMAL -> 0.75f
            Difficulty.HARD -> 1.05f
            Difficulty.ERECT -> 1.35f
            Difficulty.NIGHTMARE -> 1.65f
        }

        // FNF rhythm patterns for each section
        for (measure in 0 until totalMeasures) {
            val measureStartMs = measure * 16 * stepMs

            // 16 steps per measure (4 beats * 4 steps)
            // Call section (steps 0..7) = Opponent
            // Response section (steps 8..15) = Player
            // Or alternating measures
            val isPlayerLead = (measure % 4 >= 2)

            val phraseSteps = listOf(0, 2, 4, 6, 8, 10, 12, 14)
            val syncopatedSteps = listOf(1, 3, 5, 7, 9, 11, 13, 15)

            // Opponent phrase
            val oppSectionOffset = if (isPlayerLead) 8 * stepMs else 0f
            val plySectionOffset = if (isPlayerLead) 0f else 8 * stepMs

            // Opponent notes
            for (step in 0..7) {
                if (shouldPlaceNote(step, density, measure)) {
                    val strum = (measureStartMs + oppSectionOffset + (step * stepMs)).toLong()
                    val dir = NoteDirection.fromIndex((step + measure * 2) % 4)
                    notes.add(GameNote(noteId++, strum, dir, length = if (step % 4 == 0 && difficulty != Difficulty.EASY) (stepMs * 1.5f).toLong() else 0L, isPlayer = false))
                }
            }

            // Player response notes
            for (step in 0..7) {
                if (shouldPlaceNote(step, density, measure)) {
                    val strum = (measureStartMs + plySectionOffset + (step * stepMs)).toLong()
                    val dir = NoteDirection.fromIndex((step + measure * 3 + 1) % 4)
                    val sustain = if (step % 3 == 0 && difficulty >= Difficulty.HARD) (stepMs * 2f).toLong() else 0L
                    notes.add(GameNote(noteId++, strum, dir, length = sustain, isPlayer = true))
                }
            }

            // For HARD / ERECT / NIGHTMARE: add simultaneous or fast stream notes on upbeat
            if (difficulty >= Difficulty.HARD && measure % 2 == 1) {
                val burstStrum = (measureStartMs + plySectionOffset + (4 * stepMs)).toLong()
                notes.add(GameNote(noteId++, burstStrum, NoteDirection.UP, isPlayer = true))
            }
        }

        return notes.sortedBy { it.strumTime }
    }

    private fun shouldPlaceNote(step: Int, density: Float, measure: Int): Boolean {
        // Base downbeats (0, 4) always have higher probability
        if (step == 0 || step == 4) return true
        if (step == 2 || step == 6) return density >= 0.6f
        // Syncopation
        val r = ((step * 37 + measure * 19) % 100) / 100f
        return r < (density * 0.5f)
    }
}
