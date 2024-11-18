Feature: Example feature file

  Background:
    Given initial setup is complete
    And kafka is up and running
    And postgres is up and running
    And the service is up and running
    And now is "2024-11-06T08:13:12.345"

  @Positive
  Scenario: Successfully create an order

    When a POST request is sent to "/orders" with data
    """
      {
        "purchaseProductDTOS": [
          {
            "productId": 1,
            "quantity": 2,
            "price": 29.99
          },
          {
            "productId": 2,
            "quantity": 1,
            "price": 59.49
          }
        ],
        "city": "London"
      }
    """

    Then a successful response is generated with a 201 status and a body similar to
    """
      {
        "message": null,
        "error": null,
        "result": {
          "orderId": "a3e16dc7-0c39-4e30-9d53-529bacf2f3e1",
          "status": "CREATED"
        },
        "timestamp":"2024-11-06T18:14:59.346148"
       }
    """

    And contains headers
      | Location | http://localhost/orders/612d01fd-9c2b-4da3-b309-35743ed6a050" |

    And an order is saved to the database
    And the order is sent to the kafka topic "orders"

  @Positive
  Scenario: Successfully update an order
    Given an order exists in the database with the following data
    """
      {
        "id": 1,
        "orderId":"fdbb15ad-db2b-4b7a-b900-b05affa904b9",
        "price": 25.50,
        "productIds": "1,2,3",
        "status": "CREATED",
        "city": "London",
        "creationDate": "2024-11-18T20:02:42.413939",
        "lastUpdated": "2024-11-18T20:02:42.413939",
        "auditItems": []
      }
    """

    When a PUT request is sent to "/orders/order/fdbb15ad-db2b-4b7a-b900-b05affa904b9/PENDING_DELIVERY"

    Then a successful response is generated with a 200 status and a body similar to
    """
      {
        "message": null,
        "error": null,
        "result": {
          "orderId": "a3e16dc7-0c39-4e30-9d53-529bacf2f3e1",
          "status": "PENDING_DELIVERY"
        },
        "timestamp":"2024-11-06T18:14:59.346148"
       }
    """

    And the order in the database should be
    """
      {
        "id": 1,
        "orderId":"fdbb15ad-db2b-4b7a-b900-b05affa904b9",
        "price": 25.50,
        "productIds": "1,2,3",
        "status": "PENDING_DELIVERY",
        "city": "London",
        "creationDate": "2024-11-18T20:02:42.413939",
        "lastUpdated": "2024-11-18T20:02:42.413939",
        "auditItems": [
          {
            "status": "CREATED",
            "lastUpdated": "2024-11-18T20:02:42.413939"
          }
        ]
      }
    """

  @Negative
  Scenario: Fails to create an order because of missing product ids
    When a POST request is sent to "/orders" with data
    """
      {
        "purchaseProductDTOS": [],
        "city": "London"
      }
    """

    Then an error response is generated with a 400 status and a body similar to
    """
      {
        "message" : null,
        "error" : "An order cannot be created with no products",
        "result" : null,
        "timestamp" : "2024-11-08T22:42:40.522034"
      }
    """

    And an order is not saved to the database
    And the order is not sent to the kafka topic "orders"