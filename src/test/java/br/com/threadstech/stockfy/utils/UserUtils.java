package br.com.threadstech.stockfy.utils;

import br.com.threadstech.stockfy.enums.Role;

public class UserUtils {

  public static User admin = new User("admin", Role.ADMIN);
  public static User inventoryManager = new User("inventory manager", Role.INVENTORY_MANAGER);
  public static User salesAttendant = new User("sales attendant", Role.SALES_ATTENDANT);

  public record User(String name, Role role) {}
}
