import java.util.Random;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BlackJack {
    private class Card {
        String value;
        String suit;

        Card(String value, String suit) {
            this.value = value;
            this.suit = suit;
        }

        @Override
        public String toString() {
            return value + "-" + suit;
        }

        public int getCardValue() {
            switch (value) {
                case "A":
                    return 11;
                case "K":
                case "Q":
                case "J":
                    return 10;
                default:
                    return Integer.parseInt(value);
            }
        }

        // public boolean isAce() {
        // return value.equals("A");
        // }

        // public String getImagePath() {
        // return "./cards/" + toString() + ".png";
        // }
    }

    ArrayList<Card> deck;
    Random deckRandomizer = new Random(); // Object for shuffle

    // Logic for the Dealer.
    Card faceDownCard;
    ArrayList<Card> dealerHand = new ArrayList<Card>();
    int houseTotal;
    int houseAceCount; // Needed for changing values of Aces.

    // Logic for the Player. (YOU!)
    ArrayList<Card> playerHand = new ArrayList<Card>();
    int playerTotal;
    int playerAceCount; // Needed for changing values of Aces.

    // Declarations for the Game Window.
    int windowWidth = 600; // 600 x 600 window
    int windowHeight = 600;

    // Card Dimensions
    int cardWidth = 110; // Ratio is 1:1.4
    int cardHeight = 154;

    // Game Window
    JFrame window = new JFrame("BlackJack");
    JPanel panel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                // draw dealer's face-down card
                Image faceDownCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!stand.isEnabled()) {
                    faceDownCardImg = new ImageIcon(getClass().getResource("./cards/" + faceDownCard + ".png"))
                            .getImage();
                    // faceDownCardImg = new
                    // ImageIcon(getClass().getResource(faceDownCard.getImagePath())).getImage();
                }
                g.drawImage(faceDownCardImg, 20, 20, cardWidth, cardHeight, null);

                // draw dealer's face-up card
                for (int i = 0; i < dealerHand.size(); i++) {
                    String cardImgPath = "./cards/" + dealerHand.get(i) + ".png";
                    Image cardImg = new ImageIcon(getClass().getResource(cardImgPath)).getImage();
                    g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5) * i, 20, cardWidth, cardHeight, null);
                }

                // for (int i = 0; i < dealerHand.size(); i++) {
                // Card card = dealerHand.get(i);
                // Image cardImg = new
                // ImageIcon(getClass().getResource(card.getImagePath)).getImage();
                // g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5) * i, 20, cardWidth,
                // cardHeight, null);
                // }

                // draw player's cards
                for (int i = 0; i < playerHand.size(); i++) {
                    String cardImgPath = "./cards/" + playerHand.get(i) + ".png";
                    Image cardImg = new ImageIcon(getClass().getResource(cardImgPath)).getImage();
                    g.drawImage(cardImg, 20 + (cardWidth + 5) * i, 320, cardWidth, cardHeight, null);
                }

                if (!stand.isEnabled()) {
                    houseTotal = recastDealerAce();
                    playerTotal = recastAce();
                    System.out.println("STAY: ");
                    System.out.println(houseTotal);
                    System.out.println(playerTotal);

                    String declareWinner = "";
                    if (playerTotal > 21) {
                        declareWinner = "You Bust, House Wins.";
                    } else if (houseTotal > 21) {
                        declareWinner = "House Busts, You Win!";
                    } else if (playerTotal == houseTotal) {
                        declareWinner = "It's a Tie.";
                    } else if (playerTotal > houseTotal) {
                        declareWinner = "You Win!";
                    } else {
                        declareWinner = "House Wins.";
                    }
                    g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
                    g.setColor(Color.red);
                    g.drawString(declareWinner, 220, 250);
                }

                // for (int i = 0; i < playerHand.size(); i++) {
                // Card card = playerHand.get(i);
                // Image cardImg = new
                // ImageIcon(getClass().getResource(card.getImagePath)).getImage();
                // g.drawImage(cardImg, 20 + (cardWidth + 5) * i, 320, cardWidth, cardHeight,
                // null);
                // }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttons = new JPanel();
    JButton hit = new JButton("Hit");
    JButton stand = new JButton("Stand");

    BlackJack() {
        startGame();

        window.setVisible(true);
        window.setSize(windowWidth, windowHeight);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel.setLayout(new BorderLayout());
        // set background image
        panel.setBackground(new Color(20, 20, 20));
        window.add(panel);

        hit.setFocusable(false);
        buttons.add(hit);
        stand.setFocusable(false);
        buttons.add(stand);
        window.add(buttons, BorderLayout.SOUTH);

        hit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Card card = deck.remove(deck.size() - 1);
                playerTotal += card.getCardValue();
                playerAceCount += card.getCardValue() == 11 ? 1 : 0;
                // playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (recastAce() > 21) {
                    hit.setEnabled(false);
                }
                panel.repaint();
            }
        });

        stand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hit.setEnabled(false);
                stand.setEnabled(false);
                while (houseTotal < 17) {
                    Card card = deck.remove(deck.size() - 1);
                    houseTotal += card.getCardValue();
                    houseAceCount += card.getCardValue() == 11 ? 1 : 0;
                    // houseAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }
                panel.repaint();
            }
        });

        // Repaint in case of non-updated component.
        panel.repaint();
    }

    public void startGame() {
        // deck
        buildDeck();
        shuffle();
        // dealer
        dealerHand = new ArrayList<Card>();
        houseTotal = 0;
        houseAceCount = 0;
        // dealer hand
        faceDownCard = deck.remove(deck.size() - 1);
        houseTotal += faceDownCard.getCardValue();
        houseAceCount += faceDownCard.getCardValue() == 11 ? 1 : 0;
        // houseAceCount += faceDownCard.isAce() ? 1 : 0;
        Card card = deck.remove(deck.size() - 1);
        houseTotal += card.getCardValue();
        houseAceCount += card.getCardValue() == 11 ? 1 : 0;
        // houseAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);
        System.out.println("Dealer:");
        System.out.println("Face Down Card: " + faceDownCard);
        System.out.println("Card: " + card);
        System.out.println("Total: " + houseTotal);
        System.out.println("Ace Count: " + houseAceCount);

        // player
        playerHand = new ArrayList<Card>();
        playerTotal = 0;
        playerAceCount = 0;
        // player hand
        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size() - 1);
            playerTotal += card.getCardValue();
            playerAceCount += card.getCardValue() == 11 ? 1 : 0;
            // playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        System.out.println("Player:");
        System.out.println("Cards: " + playerHand);
        System.out.println("Total: " + playerTotal);
        System.out.println("Ace Count: " + playerAceCount);
    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = { "A", "2", "3", "4", "5", "6", "7", "8", "9",
                "10", "J", "Q", "K" };
        String[] suits = { "H", "D", "S", "C" };

        // for (int i = 0; i < suits.length; i++)
        // {
        // for (int j = 0; j < values.length; j++)
        // {
        // Card card = new Card(values[j], suits[i]);
        // deck.add(card);
        // }
        // }

        for (String suit : suits) {
            for (String value : values) {
                deck.add(new Card(value, suit));
            }
        }

        System.out.println("Building Deck...");
        System.out.println("Deck Size: " + deck.size());
        System.out.println("Deck: " + deck);
        System.out.println("Deck Built Successfully");
    }

    public void shuffle() {
        for (int i = 0; i < deck.size(); i++) {
            int randomIndex = deckRandomizer.nextInt(deck.size());
            Card temp = deck.get(i);
            deck.set(i, deck.get(randomIndex));
            deck.set(randomIndex, temp);
        }

        // for (int i = 0; i < deck.size(); i++) {
        // int j = deckRandomizer.nextInt(deck.size());
        // Card currentCard = deck.get(i);
        // Card randomCard = deck.get(j);
        // deck.set(i, randomCard);
        // deck.set(j, currentCard);
        // }

        System.out.println("Shuffling Deck...");
        System.out.println("Deck Size: " + deck.size());
        System.out.println(" Shuffled Deck: " + deck);
        System.out.println("Deck Shuffled Successfully");
    }

    public int recastAce() {
        int total = playerTotal;
        while (total > 21 && playerAceCount > 0) {
            total -= 10;
            playerAceCount--;
        }
        return total;
    }

    public int recastDealerAce() {
        int total = houseTotal;
        while (total > 21 && houseAceCount > 0) {
            total -= 10;
            houseAceCount--;
        }
        return total;
    }
}
