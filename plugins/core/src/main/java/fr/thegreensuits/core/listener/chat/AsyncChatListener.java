package fr.thegreensuits.core.listener.chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class AsyncChatListener implements Listener, ChatRenderer {

  @EventHandler()
  public void onAsyncChat(AsyncChatEvent event) {
    System.out.println("@message toString()" + event.message().toString());
    List<Player> mentionedPlayers = Bukkit.matchPlayer(event.message().toString());

    TextComponent.Builder messageBuilder = Component.text();

    Component messageText = event.message();
    for (Player player : mentionedPlayers) {
      String playerName = player.getName();

      Component highlightedName = Component.text(playerName, NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD);

      messageText = messageText.replaceText(configurer -> configurer.match(playerName).replacement(highlightedName));

      player.playSound(player.getLocation(),
          Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
    }

    messageBuilder.append(messageText.color(NamedTextColor.GRAY));
    event.message(messageBuilder.build());

    event.renderer(this);
  }

  @Override
  public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
    System.out.println("@render viewer" + viewer);

    return sourceDisplayName.append(Component.text(" » ", NamedTextColor.DARK_GRAY)).append(message);
  }
}
