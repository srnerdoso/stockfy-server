package br.com.threadstech.stockfy.modules.users.application.port;

public interface ResetCodeHasher {
  String hash(String rawCode);
}
