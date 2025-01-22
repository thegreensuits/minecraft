package fr.thegreensuits.survival.handler;

import fr.thegreensuits.api.player.Player;
import fr.thegreensuits.api.player.events.JoinHandler;

public class SurvivalJoinHandler implements JoinHandler {
  @Override()
  public void onJoin(Player player) {
    System.out.println(player.getUniqueId() + " a rejoint le serveur !");
    /*
     * Player player =
     * TheGreenSuits.spigot().getPlayerManager().retrieve(event.getPlayer().
     * getUniqueId());
     * Component playerDisplayName = Component.text(player.getDisplayName());
     * 
     * Component playerDisplayName = Component.text(event.getPlayer().getName(),
     * NamedTextColor.GRAY);
     * 
     * Component message = Component.text("[", NamedTextColor.DARK_GRAY)
     * .append(Component.text("+", NamedTextColor.AQUA))
     * .append(Component.text("] ", NamedTextColor.DARK_GRAY))
     * .append(Component.text(event.getPlayer().getName(), NamedTextColor.AQUA))
     * .append(Component.text(" a rejoint le serveur !", NamedTextColor.GRAY));
     * event.joinMessage(message);
     * 
     * event.getPlayer().displayName(playerDisplayName);
     * event.getPlayer().playerListName(playerDisplayName);
     */
  }
}
