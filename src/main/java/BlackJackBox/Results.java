package BlackJackBox;

import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.List;



public class Results {
    private List<Member> winners = new ArrayList<>();
    private List<Member> ties = new ArrayList<>();
    private List<Member> busts = new ArrayList<>();
    private List<Member> losers = new ArrayList<>();
    private Hand dealer;


    Results(Hand dealer) {
        this.dealer = dealer;
    }


    void addWinner(Member member) {
        winners.add(member);
    }


    void addTie(Member member) {
        ties.add(member);
    }


    void addBust(Member member) {
        busts.add(member);
    }


    void addLoser(Member member) {
        losers.add(member);
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");

        stringBuilder.append("Dealer: ");
        stringBuilder.append(dealer.toString());
        stringBuilder.append("\n");

        if (winners.size() > 0) {
            stringBuilder.append("\nWinners: ");
            for (Member member : winners) {
                stringBuilder.append(GameInstance.getName(member));
                stringBuilder.append(" ");
            }
        }

        if (ties.size() > 0) {
            stringBuilder.append("\nTies: ");
            for (Member member : ties) {
                stringBuilder.append(GameInstance.getName(member));
                stringBuilder.append(" ");
            }
        }

        if (losers.size() > 0) {
            stringBuilder.append("\nLosers: ");
            for (Member member : losers) {
                stringBuilder.append(GameInstance.getName(member));
                stringBuilder.append(" ");
            }
        }

        if (busts.size() > 0) {
            stringBuilder.append("\nBusts: ");
            for (Member member : busts) {
                stringBuilder.append(GameInstance.getName(member));
                stringBuilder.append(" ");
            }
        }

        return stringBuilder.toString();
    }
}
