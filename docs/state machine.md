I'm thrinking of implementing a finite state machine

State transitions:
- awaiting next round
- bidding
- trick taking
- trick scoring (depending on round, you could go back to trick taking)
- round scoring
- game scoring (game completed)

example:

- awaiting round 1
- bidding
- trick taking
- trick scoring
- round scoring

- awaiting round 2
- bidding
- trick taking
- trick scoring
- trick taking
- trick scoring
- round scoring

- awaiting round 3
- bidding
- trick taking
- trick scoring
- trick taking
- trick scoring
- trick taking
- trick scoring
- round scoring

etc etc up to round 10

- awaiting round 10
- bidding
- (trick taking, trick scoring) * 10
- round scoring
- game scoring
