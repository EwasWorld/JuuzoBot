package main.java.RaceBox;

import java.util.Optional;

/*
 * Used to import races from JSON file
 */
public class RaceJsonFormat {
    private RacesTemp[] races;

    public RaceJsonFormat(RacesTemp[] races) {
        this.races = races;
    }

    public RacesTemp[] getRaces() {
        return races;
    }

    class RacesTemp {
        private String name;
        private Optional<String> subrace;
        private AbilityIncreases abilityIncreases;
        private int ageUpperBound;
        private String size;
        private int speed;
        private String[] languages;

        public RacesTemp(String name, Optional<String> subrace, AbilityIncreases abilityIncreases, int ageUpperBound,
                         String size, int speed, String[] languages) {
            this.name = name;
            this.subrace = subrace;
            this.abilityIncreases = abilityIncreases;
            this.ageUpperBound = ageUpperBound;
            this.size = size;
            this.speed = speed;
            this.languages = languages;
        }

        public String getName() {
            return name;
        }

        public Optional<String> getSubrace() {
            return subrace;
        }

        public AbilityIncreases getAbilityIncreases() {
            return abilityIncreases;
        }

        public int getAgeUpperBound() {
            return ageUpperBound;
        }

        public String getSize() {
            return size;
        }

        public int getSpeed() {
            return speed;
        }

        public String[] getLanguages() {
            return languages;
        }

        class AbilityIncreases {
            private int str;
            private int dex;
            private int con;
            private int inte;
            private int wis;
            private int cha;

            public AbilityIncreases(int str, int dex, int con, int inte, int wis, int cha) {
                this.str = str;
                this.dex = dex;
                this.con = con;
                this.inte = inte;
                this.wis = wis;
                this.cha = cha;
            }

            public int getStr() {
                return str;
            }

            public int getDex() {
                return dex;
            }

            public int getCon() {
                return con;
            }

            public int getInte() {
                return inte;
            }

            public int getWis() {
                return wis;
            }

            public int getCha() {
                return cha;
            }
        }
    }
}
