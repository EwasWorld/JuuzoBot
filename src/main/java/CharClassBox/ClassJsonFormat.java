package main.java.CharClassBox;

import java.util.Optional;

/*
 * Used to import classes from JSON file
 */
public class ClassJsonFormat {
    private ClassesTemp[] classes;

    public ClassJsonFormat(ClassesTemp[] classes) {
        this.classes = classes;
    }

    public ClassesTemp[] getClasses() {
        return classes;
    }

    class ClassesTemp {
        private String name;
        private Optional<String> secondaryType;
        private int hitDie;
        private AbilityOrder abilityOrder;
        private String[] savingThrows;
        private String[] skillProficiencies;
        private Funds funds;

        public ClassesTemp(String name, Optional<String> secondaryType, int hitDie, AbilityOrder abilityOrder, String[]
                savingThrows, String[] skillProficiencies, Funds funds) {
            this.name = name;
            this.secondaryType = secondaryType;
            this.hitDie = hitDie;
            this.abilityOrder = abilityOrder;
            this.savingThrows = savingThrows;
            this.skillProficiencies = skillProficiencies;
            this.funds = funds;
        }

        public String getName() {
            return name;
        }

        public Optional<String> getSecondaryType() {
            return secondaryType;
        }

        public int getHitDie() {
            return hitDie;
        }

        public AbilityOrder getAbilityOrder() {
            return abilityOrder;
        }

        public String[] getSavingThrows() {
            return savingThrows;
        }

        public String[] getSkillProficiencies() {
            return skillProficiencies;
        }

        public Funds getFunds() {
            return funds;
        }

        class AbilityOrder {
            private int str;
            private int dex;
            private int con;
            private int inte;
            private int wis;
            private int cha;

            public AbilityOrder(int str, int dex, int con, int inte, int wis, int cha) {
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
