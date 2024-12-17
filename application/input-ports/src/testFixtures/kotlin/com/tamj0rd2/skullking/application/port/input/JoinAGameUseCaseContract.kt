package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameIsFull
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test

abstract class JoinAGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `each player who joins the game can see themself in the game`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { playerCount ->
                val gameCreator = scenario.newPlayer()
                val thisPlayer = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(playerCount - 1)

                Given {
                    gameCreator.`creates a game`()
                    gameCreator.invites(otherPlayers + thisPlayer)
                }

                When { thisPlayer.`accepts the game invite`() }

                Then { thisPlayer.`sees them self in the game`() }
            }
        }

    @Test
    fun `each player who joins the game can see the other players who have joined`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { otherPlayerCount ->
                val gameCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(otherPlayerCount)

                Given {
                    gameCreator.`has created a game`()
                    gameCreator.invites(otherPlayers)
                }

                When {
                    otherPlayers.each { `accept the game invite`() }
                }

                Then {
                    otherPlayers.each { `sees exact players in the game`(gameCreator + otherPlayers) }
                }
            }
        }

    @Test
    fun `cannot join a full game`() {
        val gameCreator = scenario.newPlayer()
        val playersWhoWillJoinFirst = scenario.newPlayers(MAXIMUM_PLAYER_COUNT - 1)
        val latePlayer = scenario.newPlayer()

        Given {
            gameCreator.`has created a game`()
            gameCreator.invites(playersWhoWillJoinFirst + latePlayer)
            playersWhoWillJoinFirst.each { `accept the game invite`() }
        }

        When {
            latePlayer.triesTo { `accept the game invite`() }
        }

        Then {
            latePlayer.`gets the error`(GameIsFull())
        }
    }

    @Test
    fun `a player cannot join a game they're already in`() {
        val theGameCreator = scenario.newPlayer()
        val thisPlayer = scenario.newPlayer()

        Given {
            theGameCreator.`has created a game`()
            theGameCreator.invites(thisPlayer)
            thisPlayer.`accepts the game invite`()
        }

        When {
            thisPlayer.triesTo { `accept the game invite`() }
        }

        Then { thisPlayer.`gets the error`(PlayerHasAlreadyJoined()) }
    }

    @Test
    fun `a player cannot join a game that has already started`() =
        propertyTest {
            checkAll(Arb.int(min = 1, max = MAXIMUM_PLAYER_COUNT - 1)) { playerCount ->
                val theGameCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(playerCount)
                val lateJoiningPlayer = scenario.newPlayer()

                Given {
                    theGameCreator.`has created a game`()
                    theGameCreator.invites(otherPlayers + lateJoiningPlayer)
                    otherPlayers.each { `accept the game invite`() }
                    theGameCreator.`starts the game`()
                }

                When {
                    lateJoiningPlayer.triesTo { `accept the game invite`() }
                }

                Then {
                    lateJoiningPlayer.`gets the error`(GameHasAlreadyStarted())
                }
            }
        }
}
