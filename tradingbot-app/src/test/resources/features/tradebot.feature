Feature: TradeBotApp Functionality

Scenario: Cache saves history
  Given I have empty cache
  When I added history
  Then I will be able to read history