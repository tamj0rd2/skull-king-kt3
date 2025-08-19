# Skull King UI Design Considerations

This document captures all the clarifying questions and design considerations that came up during the development of the
Skull King game UI prototypes.

## Initial Game Prototype Questions

**General Setup:**

- What card game is this for? (Answered: Skull King)
- Static UI only or interactive functionality needed? (Answered: Static UI prototypes)
- Specific features or screens needed? (Answered: Lobby, bidding, trick-taking screens)

## Lobby Screens

**Theming:**

- How many different themes/visual styles? (Answered: Multiple themes - pirate, modern, medieval)
- Should themes reflect the pirate nature of Skull King?
- Color schemes and visual aesthetics for each theme?

**Lobby List Features:**

- What information should each lobby show? (Game status, player count, round progress, etc.)
- How should lobbies be sorted/filtered?
- What actions can players take? (Join, spectate, create new)

**Join/Create Lobby:**

- What settings should be configurable when creating a lobby?
- How much information should be shown about existing lobbies before joining?
- Should there be different lobby types (public/private, skill levels)?

## Bidding Screens

**Bidding Mechanics:**

- How should the bidding interface work? (Answered: Bid selection with confirmation)
- What information do players need to see during bidding? (Cards in hand, trump, round info)
- Should there be different states shown? (Answered: Yes - no bids, partial bids, all bids placed)

**Information Display:**

- How much trump information to show?
- Should player order be displayed?
- How to handle the progression from no bids to all bids placed?

**State Management:**

- How many different bidding states to prototype? (Answered: 3 states)
- What should happen after all bids are placed?

## Trick-Taking Screens

**Card Display:**

- How to show card hierarchy and suits? (Answered: Color-coded suits, special card styling)
- Should unplayable cards be visually distinct? (Answered: Yes, dimmed/grayed out)
- How to display special cards (Skull King, Pirates, Mermaids, Scary Mary, Escape)?

**Special Card Interactions:**

- How should Scary Mary choice be handled? (Answered: Modal dialog with Pirate/Escape options)
- Should there be confirmation for card plays? (Answered: Yes, confirmation modal)
- How to show when Scary Mary is played "as Pirate" vs "as Escape"?

**Game State Display:**

- What player information is essential? (Bids, tricks won, current turn)
- How to show trick progression and card play order?
- How much round/game context to provide?

**Trick States:**

- How many different trick states to show? (Answered: Start, in-progress, complete)
- Should trick complete be a separate screen or transition? (Answered: Separate screen)
- How to visualize the winner of each trick?

**Card Count Display:**

- Should individual card counts be shown for each player? (Answered: No - all players start with same count, each plays
  one card)

## Round Complete Screen

**Scoring Display:**

- How to present round scoring? (Answered: Show calculation breakdown like "Bid 4, Made 4: +40 points")
- Should it show points earned this round vs total cumulative score? (Answered: Both)
- How to break down scoring for successful vs failed bids?

**Player Information:**

- How to show bid accuracy? (Answered: Visual indicators - claps for success, thumbs down for failure)
- Should players be ranked/ordered in any way? (Answered: Keep original player order)
- What round summary information is valuable?

**Round Context:**

- Should next round information be previewed?
- How much game progress context to show?
- Should the entire game scorecard be visible? (Answered: Yes, in sidebar)

**Actions:**

- What can players do from round complete screen? (Answered: Automatically proceeds to next round or game end)
- Should there be manual progression options?

## Game Complete Screen

**Winner Presentation:**

- How should the game winner be announced? (Answered: Big celebration with animations)
- Should final rankings be shown for all players? (Answered: Yes, 1st through 6th place)
- How to handle different win scenarios (close game vs runaway victory)?

**Final Scoring:**

- Just final totals or complete game history? (Answered: Both - rankings and full scorecard)
- What additional game statistics would be interesting? (Answered: Bid accuracy, most tricks, biggest scores, special
  card stats)

**Visual Treatment:**

- What kind of celebration for the winner? (Answered: Confetti, crown animation, golden highlighting, shimmer effects)
- Should there be different treatments for 1st/2nd/3rd place?

**Player Actions:**

- What options should be available at game end? (Answered: Single "Return to Lobby" option)
- Should there be rematch, new game, or stats viewing options?

## Technical and Design Considerations

**Responsive Design:**

- How should layouts work on different screen sizes?
- What's the primary target device/screen size?

**Accessibility:**

- What accessibility features are needed?
- How to ensure good contrast and readability?
- Should there be keyboard navigation support?

**Consistency:**

- How to maintain visual consistency across all screens?
- What design system/style guide to follow?
- How to handle state transitions and animations?

**Theme Implementation:**

- How different should the themes actually be?
- Which theme should be the primary/default?
- Should functionality change between themes or just styling?

## Information Architecture

**Player Status:**

- What player information is essential vs nice-to-have?
- How to show current turn, waiting states, actions taken?
- How much historical information to display?

**Game Flow:**

- How should screens transition between each other?
- What information needs to persist across screens?
- How to handle back navigation and game state?

**Error States:**

- How to handle invalid actions or game states?
- What feedback should players receive for their actions?
- How to handle disconnections or technical issues?

## Visual Hierarchy

**Information Priority:**

- What information is most critical for player decisions?
- How to balance game state info with action options?
- What deserves the most visual prominence on each screen?

**Action Clarity:**

- How to make available actions obvious?
- What confirmation patterns to use for important actions?
- How to prevent accidental actions?

## Content Strategy

**Text and Messaging:**

- What tone should the interface use?
- How much explanation vs assumption of game knowledge?
- What success/feedback messages are needed?

**Iconography:**

- What icons/symbols are universally understood?
- How to represent game-specific concepts visually?
- Should emojis be used or more traditional icons?

---

## Exact Questions Asked During Development

### Initial Lobby Screens Questions:

"I'd be happy to help you create static UI prototypes for a card game! To make sure I design exactly what you need, let
me ask a few clarifying questions:

1. **Game Context**: What card game is this for? This will help me understand the specific mechanics and terminology to
   include.

2. **Screen Types**: You mentioned lobby list and join lobby screens - are there any other key screens you'd like
   prototypes for? (For example: game setup, player hand, scoring, etc.)

3. **Visual Style**: Do you have any preferences for the visual theme or style? (Modern, classic, fantasy, etc.)

4. **Functionality Level**: Should these be purely visual prototypes, or do you want some basic interactivity (like
   clickable buttons that show hover states)?

5. **Platform**: Are these for web, mobile, desktop app, or should they be responsive for multiple platforms?

6. **Player Count**: How many players does this game support? This will affect lobby capacity and UI layout.

Let me know these details and I'll create some great prototypes for you!"

### Lobby Theming Questions:

"Great choice with Skull King! I love that game. Let me create some lobby prototypes for you.

A few quick questions to nail the design:

1. **Themes**: You mentioned different theming - should I create versions that reflect the pirate theme of Skull King (
   nautical/pirate aesthetic), or would you prefer more neutral/modern themes, or a mix of both?

2. **Lobby Information**: For the lobby list, what key information should each lobby show? I'm thinking:
    - Lobby name
    - Player count (current/max)
    - Game status (waiting, in progress, etc.)
    - Host name
    - Maybe current round if game is in progress?

3. **Player Actions**: What actions should be available?
    - Join lobby
    - Create new lobby
    - Spectate ongoing games?
    - Filter/search lobbies?

4. **Lobby Size**: What's the standard player count for Skull King lobbies? (I believe it's typically 2-8 players)

I'll create a few different themed versions so you can see different aesthetic approaches!"

### Bidding Screen Questions:

"Perfect! I'm glad you liked the modern theme. Now let's create a bidding screen prototype for Skull King.

Let me ask a few clarifying questions about the bidding phase:

1. **Bidding Information**: What information should be visible during bidding?
    - Player's cards in hand?
    - Current round number and total cards dealt?
    - Trump suit (if revealed)?
    - Other players' names and bidding status?

2. **Bidding Interface**: How should the actual bidding work?
    - Dropdown/slider for bid amount?
    - Click buttons for 0, 1, 2, 3... up to max possible?
    - Should there be a confirmation step?

3. **Bidding States**: Should I show different states?
    - Before anyone has bid?
    - Partially through bidding (some players have bid, others haven't)?
    - All bids placed, ready to start tricks?

4. **Round Context**: Should it show what round this is and how many cards each player has?

5. **Trump Display**: How prominently should the trump suit be displayed? Is it revealed before or during bidding in
   your version?

Let me know your preferences and I'll create a comprehensive bidding screen!"

### Trick-Taking Screen Questions:

"Excellent! I'm glad the bidding screens work well for you. Now let's tackle the trick-taking screen, which is probably
the most complex part of the Skull King interface.

Let me ask some detailed questions about the trick-taking phase:

1. **Card Display**: How should the cards be shown?
    - Should I show actual card faces with suits/numbers, or placeholder representations?
    - How to distinguish special cards (Skull King, Pirates, Mermaids, Scary Mary, Escape)?
    - Should unplayable cards be visually dimmed/disabled?

2. **Trick Area**: How should the current trick be displayed?
    - Cards played in the center in play order?
    - Show which player played each card?
    - Indicate the current winning card somehow?

3. **Player Information**: What should be shown for each player?
    - Names and current scores/tricks won?
    - Their bid for this round?
    - Indication of whose turn it is?
    - How many cards left in hand?

4. **Special Card Interactions**:
    - Should there be a modal/popup for Scary Mary choice (Pirate vs Escape)?
    - How to show when someone plays a special card?
    - Confirmation dialogs for card plays?

5. **Game State**: What context should be visible?
    - Current round number and trick number?
    - Trump suit indicator?
    - Lead suit for current trick?

6. **Hand Management**: How should the player's hand be displayed?
    - Fan of cards at bottom?
    - How to indicate which cards are playable vs not playable?

I want to make sure I capture all the important Skull King mechanics properly!"

### Final Trick-Taking Clarification Questions:

"Perfect! Those are really helpful details about the Skull King rules and interactions. Let me ask a few final
clarifying questions to nail the trick-taking interface:

1. **Card Representations**: Since you mentioned no real card art, should I use:
    - Simple text labels like "Red 14", "Blue 8", "Skull King", "Pirate"?
    - Basic geometric shapes with numbers?
    - Emojis/icons for special cards (💀 for Skull King, 🏴‍☠️ for Pirate, etc.)?

2. **Unplayable Card Feedback**: When cards are dimmed because they can't be played, should there be any hover tooltip
   or message explaining why? (Like "Must follow lead suit" or "Cannot play this card")?

3. **Trick Visualization**: For the center trick area, should I show:
    - Just the cards currently played this trick?
    - Or also some indication of previous trick winners?
    - Play order numbers on the cards (1st, 2nd, 3rd played)?

4. **Scary Mary Modal**: When Scary Mary is selected, should the modal:
    - Show the Scary Mary card prominently?
    - Explain what each choice (Pirate vs Escape) means?
    - Have confirm/cancel options?

5. **Multiple Prototypes**: Should I create separate prototypes for different moments?
    - Start of trick (no cards played yet)
    - Middle of trick (some cards played)
    - End of trick (all cards played, showing winner)

6. **Round Context**: You mentioned Round 9 - should I show this as a high-stakes round where players have lots of cards
   and the scoring is significant?

These details will help me create the most accurate and useful prototypes!"

### Round Complete Screen Questions:

"Great idea! A round completed screen would be perfect to show after all tricks in a round are finished. Let me ask some
clarifying questions to make sure I design this correctly:

## Round Completion Context Questions:

1. **Scoring Display**: How should the scoring be presented?
    - Show points earned this round vs. total cumulative score?
    - Should it break down scoring (e.g., "Bid 4, Made 4: +40 points" vs "Bid 3, Made 1: -20 points")?

2. **Round Summary**: What round information should be highlighted?
    - Show the final trump suit that was used?
    - Display a summary of who won the most tricks?
    - Show bid accuracy (who made their bid vs. who didn't)?

3. **Player Rankings**: Should there be any ranking or ordering?
    - Order players by points earned this round?
    - Order by total cumulative score?
    - Or keep the original player order?

4. **Next Round Preview**: What information about the upcoming round?
    - Show that Round 10 is next (fewer cards)?
    - Preview the new dealer/starting player?
    - Any indication of how many rounds remain in the game?

5. **Action Options**: What should players be able to do?
    - "Continue to Next Round" button?
    - "View Game Statistics" or "View Round History"?
    - "Leave Game" option?

6. **Visual Style**: Any special visual treatment?
    - Highlight the round winner (highest points this round)?
    - Different styling for players who made vs. missed their bids?
    - Keep the same modern theme as the other screens?

7. **Transition Context**: Where does this screen appear in the flow?
    - After the final trick of the round is complete?
    - Should it replace the trick-complete screen or be a separate transition?

Let me know your preferences and I'll create a round completed screen that fits perfectly with your game flow!"

### Game Complete Screen Questions:

"Excellent! Those are great clarifications. Let me create a round completed screen with all those features. I'll also
note the feedback about the bidding screen needing to show which player goes first in tricks - we can address that
later.

Perfect! A game completed screen to wrap up the entire Skull King game. Let me ask some clarifying questions to design
this perfectly:

## Game Completion Context Questions:

1. **Winner Announcement**: How should the game winner be presented?
    - Big celebration for the winner with special animations/effects?
    - Show final ranking of all players (1st, 2nd, 3rd, etc.)?
    - Highlight if you won vs. if someone else won?

2. **Final Scoring Display**: What scoring information should be shown?
    - Just final totals, or also show the complete scorecard from all rounds?
    - Highlight the winning score vs. point spread?
    - Show any interesting game statistics (most successful bids, biggest round, etc.)?

3. **Game Statistics**: What additional stats might be interesting?
    - Bid accuracy percentage for each player across the whole game?
    - Most tricks won in a single round?
    - Best/worst round performance?
    - Who had the most Skull King captures, Pirate wins, etc.?

4. **Player Actions**: What should players be able to do?
    - "Play Again" button (same players, new game)?
    - "New Game" (different setup)?
    - "View Detailed Stats" or "Game Replay"?
    - "Save Game Results" or "Share Results"?
    - "Return to Lobby"?

5. **Visual Celebration**: What kind of special treatment for the winner?
    - Confetti or particle effects?
    - Trophy/crown imagery?
    - Special color scheme or highlighting?
    - Victory music indication or sound effects mention?

6. **Game Context**: Any specific end-game information?
    - Show the final round that determined the winner?
    - Mention if it was a close game vs. a runaway victory?
    - Show how long the game took (if that's tracked)?

7. **Transition Context**: Where do players go from here?
    - Back to the lobby system?
    - Immediate rematch option?
    - Game history/stats archive?

Let me know your preferences and I'll create an engaging game completion screen that gives players a satisfying end to
their Skull King experience!"
