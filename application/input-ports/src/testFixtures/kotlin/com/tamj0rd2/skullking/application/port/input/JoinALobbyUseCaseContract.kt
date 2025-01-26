package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.testsupport.Given
import com.tamj0rd2.skullking.application.port.input.testsupport.Then
import com.tamj0rd2.skullking.application.port.input.testsupport.UseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.When
import com.tamj0rd2.skullking.application.port.input.testsupport.each
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.LobbyIsFull
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.Lobby.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test

interface JoinALobbyUseCaseContract : UseCaseContract {
    @Test
    fun `each player who joins the lobby can see themself in the lobby`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { playerCount ->
                val gameCreator = scenario.newPlayer()
                val thisPlayer = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(playerCount - 1)

                Given {
                    gameCreator.`creates a lobby`()
                    gameCreator.invites(otherPlayers + thisPlayer)
                }

                When { thisPlayer.`accepts the lobby invite`() }

                Then { thisPlayer.`sees them self in the lobby`() }
            }
        }

    @Test
    fun `each player who joins the lobby can see the other players who have joined`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { otherPlayerCount ->
                val gameCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(otherPlayerCount)

                Given {
                    gameCreator.`has created a lobby`()
                    gameCreator.invites(otherPlayers)
                }

                When {
                    otherPlayers.each { `accept the lobby invite`() }
                }

                Then {
                    otherPlayers.each { `sees exact players in the lobby`(gameCreator + otherPlayers) }
                }
            }
        }

    @Test
    fun `cannot join a full lobby`() {
        val gameCreator = scenario.newPlayer()
        val playersWhoWillJoinFirst = scenario.newPlayers(MAXIMUM_PLAYER_COUNT - 1)
        val latePlayer = scenario.newPlayer()

        Given {
            gameCreator.`has created a lobby`()
            gameCreator.invites(playersWhoWillJoinFirst + latePlayer)
            playersWhoWillJoinFirst.each { `accept the lobby invite`() }
        }

        When {
            latePlayer.triesTo { `accept the lobby invite`() }
        }

        Then {
            latePlayer.`gets the error`(LobbyIsFull())
        }
    }

    @Test
    fun `a player cannot join a lobby they're already in`() {
        val theLobbyCreator = scenario.newPlayer()
        val thisPlayer = scenario.newPlayer()

        Given {
            theLobbyCreator.`has created a lobby`()
            theLobbyCreator.invites(thisPlayer)
            thisPlayer.`accepts the lobby invite`()
        }

        When {
            thisPlayer.triesTo { `accept the lobby invite`() }
        }

        Then { thisPlayer.`gets the error`(PlayerHasAlreadyJoined()) }
    }

    @Test
    fun `a player cannot join a lobby where the game has already started`() =
        propertyTest {
            checkAll(Arb.int(min = 1, max = MAXIMUM_PLAYER_COUNT - 1)) { playerCount ->
                val theLobbyCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(playerCount)
                val lateJoiningPlayer = scenario.newPlayer()

                Given {
                    theLobbyCreator.`has created a lobby`()
                    theLobbyCreator.invites(otherPlayers + lateJoiningPlayer)
                    otherPlayers.each { `accept the lobby invite`() }
                    theLobbyCreator.`starts the game`()
                }

                When {
                    lateJoiningPlayer.triesTo { `accept the lobby invite`() }
                }

                Then {
                    lateJoiningPlayer.`gets the error`(GameHasAlreadyStarted())
                }
            }
        }
}
