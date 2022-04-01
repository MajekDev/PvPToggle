package dev.majek.pvptoggle.hook;

import dev.majek.pvptoggle.PvPToggle;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.*;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LuckPermsHook {

  private LuckPerms lp;

  public LuckPermsHook() {
    try {
      this.lp = LuckPermsProvider.get();
    } catch (final IllegalStateException ex) {
      this.lp = null;
      PvPToggle.error("Error hooking into LuckPerms");
      ex.printStackTrace();
    }
  }

  public void registerContexts() {
    if (this.lp == null) {
      PvPToggle.error("Couldn't register contexts with LuckPerms!");
      return;
    }
    this.lp.getContextManager().registerCalculator(new ContextCalculator<Player>() {
      @Override
      public void calculate(@NonNull Player target, @NonNull ContextConsumer consumer) {
        consumer.accept("pvptoggle-status", String.valueOf(PvPToggle.userHandler().getUser(target).pvpStatus()));
      }

      @Override
      public @NonNull ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        builder.add("pvptoggle-status", "true");
        builder.add("pvptoggle-status", "false");
        return builder.build();
      }
    });
  }
}
