FORMAT: 1A
HOST: http://startup.cloudapp.net:8080/Friends24Server/rest/

# Friends24
Friends24 API is a Servis24 API for Facebook. All request are secured by HTTP Basic Authetication.
Uses HTTP error codes when problems happened (200 OK, 400 bad request, 403 forbidden, 404 resource not found, 500 internal server error).


## User Profile [/userProfile]
### User profile for authenticated user. Just for check successful authentication and authorization to services interface. [GET]
+ Response 200 (application/json)

        {
            "username": "jane",
            "password": "[PROTECTED]"
        }
        
### Update facebook id of current user. Allows linkng user with facebook directly through API. [POST]
+ Request (application/json)

        {
            "facebookId": "123123"
        }
+ Response 200  


## Accounts Collection [/accounts]
### List all user bank accounts [GET]
+ Response 200 (application/json)

        {"accounts": [
            {
                "id": 50000,
                "accountNumber": 1919,
                "accountPrefix": 19,
                "username": "jane",
                "type": 1,
                "currency": "CZK",
                "balance": 1000.2
            },
            {
                "id": 50050,
                "accountNumber": 123123,
                "accountPrefix": null,
                "username": "jane",
                "type": 2,
                "currency": "CZK",
                "balance": 5600
            }
        ]}

        
## Payments Overview [/payments/{senderAccount}]
### List of user payments with its current status [GET]
+ Parameters
    + senderAccount (number) ... sender account
+ Response 200 (application/json)

        {"payments": [
            {
                "id": "Uc4t45",
                "created": 1400151434051,
                "senderAccount": 50000,
                "recipientId": "123123",
                "recipientName": "Pepa Kotva",
                "amount": 125,
                "currency": "CZK",
                "note": "Pizza",
                "status": 0
            },
            {
                "id": "n89bOr",
                "created": 1400151585846,
                "senderAccount": 50000,
                "recipientId": "456456",
                "recipientName": "Iveta Svobodová",
                "amount": 1500,
                "currency": "CZK",
                "note": "Doplatek za hory",
                "status": 1
            },
            {
                "id": "ZKVER2",
                "created": 1400151653683,
                "senderAccount": 50000,
                "recipientId": "789789",
                "recipientName": "František Baloun",
                "amount": 2455,
                "currency": "CZK",
                "note": null,
                "status": 2
            }
        ]}


## Payment Order [/addPaymentOrder]
### Add new payment order [POST]
+ Request (application/json)

        {
            "senderAccount": 1,
            "recipientId": "123123",
            "recipientName": "Pepa Kotva",
            "amount": 125,
            "currency": "CZK",
            "note": "Pizza"
        }

+ Response 200 (application/json)

        {
            "id": "ZKVER2",
            "created": 1400151653683,
            "senderAccount": 1,
            "recipientId": "123123",
            "recipientName": "Pepa Kotva",
            "amount": 125,
            "currency": "CZK",
            "note": "Pizza",
            "status": 0
        }
        
## Collections Overview [/collections/{collectionAccount}]
### List of user collections with its current status [GET]
+ Parameters
    + collectionAccount (number) ... collectionAccount account
+ Response 200 (application/json)

        {
            "collections": [
                {
                    "id": "p91MhM",
                    "created": 1401443627097,
                    "dueDate": 1404287227923,
                    "collectionEmailParticipants":       [
                          {
                    "id": 50400,
                    "email": "lmencl@csas.cz",
                    "amount": 150,
                    "status": 0
                    },
                          {
                    "id": 50401,
                    "email": "fbaloun@csas.cz",
                    "amount": 800,
                    "status": 1
                    }
                    ],
                    "collectionFBParticipants":       [
                          {
                    "id": 50451,
                    "fbUserId": "100008185230476",
                    "fbUserName": "Honza Sechovec",
                    "amount": 100,
                    "status": 2
                    },
                          {
                    "id": 50450,
                    "fbUserId": "100008118484657",
                    "fbUserName": "Iveta Svobodová",
                    "amount": 254,
                    "status": 3
                    }
                    ],
                    "collectionAccount": 50000,
                    "targetAmount": 4000,
                    "currency": "CZK",
                    "name": "Super helma na mašinu",
                    "description": "Helma s navigací",
                    "link": "http://livemap.info/",
                    "hasImage": true
                },
                {
                    "id": "I8JGE1",
                    "created": 1401443628240,
                    "dueDate": 1400151434051,
                    "collectionEmailParticipants":       [
                              {
                        "id": 50402,
                        "email": "lmencl@csas.cz",
                        "amount": 150,
                        "status": 0
                     },
                              {
                        "id": 50403,
                        "email": "fbaloun@csas.cz",
                        "amount": 800,
                        "status": 1
                     }
                    ],
                    "collectionFBParticipants":       [
                              {
                        "id": 50453,
                        "fbUserId": "100008118484657",
                        "fbUserName": "Iveta Svobodová",
                        "amount": 254,
                        "status": 3
                     },
                              {
                        "id": 50452,
                        "fbUserId": "100008185230476",
                        "fbUserName": "Honza Sechovec",
                        "amount": 100,
                        "status": 2
                     }
                    ],
                    "collectionAccount": 50000,
                    "targetAmount": 7000,
                    "currency": "CZK",
                    "name": "Dárek pro Honzu Dárek pro Honzu",
                    "description": "Honza slaví třícátiny, přispěste prosím na houpacího koně",
                    "link": "http://www.zadaranabytek.cz/detail/4115-detsky-houpaci-kun-ad260.html?gclid=CNOPjLW8t74CFZclvQodPiMAxg",
                    "hasImage": false
            }
        ]}        
        
## Collection Image [/collections/{id}/image{?size}]
### Returns user image associated with collection [GET]
+ Parameters
    + id (string) ... id of collection
    + size (optional, number) ... max size of side in px (image is resized with preserving the aspect ratio)
    
+ Response 200 (image/jpeg)    

        /9j/4AAQSkZJRgABAQAAAQABAAD/4QxwaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hw
        YWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/Pgo8eDp4bXBt
        ZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJYTVAgQ29yZSA1LjUuMCI+CiAg
        IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3lu
        dGF4LW5zIyI+CiAgICAgIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAg
        IHhtbG5zOnhtcE1NPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvbW0vIgogICAgICAgICAg
        ICB4bWxuczpzdFJlZj0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL3NUeXBlL1Jlc291cmNl
        UmVmIyIKICAgICAgICAgICAgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAv
        Ij4KICAgICAgICAgPHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD54bXAuZGlkOjJmMmJjN2VlLTJh
        OWEtNDc4Mi1hY2Y1LWI2ZjMzYjFkMWUzZjwveG1wTU06T3JpZ2luYWxEb2N1bWVudElEPgogICAg
        ICAgICA8eG1wTU06RG9jdW1lbnRJRD54bXAuZGlkOjk1RjkzNjc5Q0ZEQzExRTM4MUUyRTA4NkRF
        REYyOENCPC94bXBNTTpEb2N1bWVudElEPgogICAgICAgICA8eG1wTU06SW5zdGFuY2VJRD54bXAu
        aWlkOjk1RjkzNjc4Q0ZEQzExRTM4MUUyRTA4NkRFREYyOENCPC94bXBNTTpJbnN0YW5jZUlEPgog
        ICAgICAgICA8eG1wTU06RGVyaXZlZEZyb20gcmRmOnBhcnNlVHlwZT0iUmVzb3VyY2UiPgogICAg
        ICAgICAgICA8c3RSZWY6aW5zdGFuY2VJRD54bXAuaWlkOjg4ZmQ0YzM0LTUyMmMtNDZiMi05MGYx
        LWY0ZjUwMGYxNDdiZTwvc3RSZWY6aW5zdGFuY2VJRD4KICAgICAgICAgICAgPHN0UmVmOmRvY3Vt
        ZW50SUQ+eG1wLmRpZDoyZjJiYzdlZS0yYTlhLTQ3ODItYWNmNS1iNmYzM2IxZDFlM2Y8L3N0UmVm
        OmRvY3VtZW50SUQ+CiAgICAgICAgIDwveG1wTU06RGVyaXZlZEZyb20+CiAgICAgICAgIDx4bXA6
        Q3JlYXRvclRvb2w+QWRvYmUgUGhvdG9zaG9wIENDIChNYWNpbnRvc2gpPC94bXA6Q3JlYXRvclRv
        b2w+CiAgICAgIDwvcmRmOkRlc2NyaXB0aW9uPgogICA8L3JkZjpSREY+CjwveDp4bXBtZXRhPgog
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
        ICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgCjw/eHBh
        Y2tldCBlbmQ9InciPz7/2wBDAAwICQoJBwwKCQoNDAwOER0TERAQESMZGxUdKiUsKyklKCguNEI4
        LjE/MigoOk46P0RHSktKLTdRV1FIVkJJSkf/2wBDAQwNDREPESITEyJHMCgwR0dHR0dHR0dHR0dH
        R0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0dHR0f/wAARCAEcARsDASIAAhEBAxEB
        /8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQID
        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
        2oA//9k=
        
### Uploads user image associated with collection [POST]
+ Parameters
    + id (string) ... id of collection
+ Request (multipart/form-data)   

        image data
    
+ Response 200        

## Collection Order [/addCollectionOrder]
### Add new collection order [POST]
+ Request (application/json)

        {
            "dueDate": 1400151434051,
            "collectionAccount": 50000,
            "targetAmount": 7000,
            "currency": "CZK",
            "name": "Dárek pro Honzu Dárek pro Honzu",
            "description": "Honza slaví třícátiny, přispěste prosím na houpacího koně",
            "link": "http://www.zadaranabytek.cz/detail/4115-detsky-houpaci-kun-ad260.html?gclid=CNOPjLW8t74CFZclvQodPiMAxg",
            "collectionEmailParticipants": [
            {
                "email": "lmencl@csas.cz",
                "amount": 150
            },
            {
                "email": "fbaloun@csas.cz",
                "amount": 800
            }
            ],
            "collectionFBParticipants": [
            {
                "fbUserId": "100008118484657",
                "fbUserName": "Iveta Svobodová",
                "amount": 254
            },
            {
                "fbUserId": "100008185230476",
                "fbUserName": "Honza Sechovec",
                "amount": 100
            }
            ]       
        }

+ Response 200 (application/json)

        {
            "id": "h9MAGQ",
            "created": 1401438606244,
            "dueDate": 1400151434051,
            "collectionEmailParticipants":
            [
                {
                "id": 50004,
                "email": "lmencl@csas.cz",
                "amount": 150,
                "status": 0
                },
                {
                "id": 50005,
                "email": "fbaloun@csas.cz",
                "amount": 800,
                "status": 0
                }
            ],
            "collectionFBParticipants":    [
                {
                "id": 50052,
                "fbUserId": "100008118484657",
                "fbUserName": "Iveta Svobodová",
                "amount": 254,
                "status": 0
                },
                {
                "id": 50053,
                "fbUserId": "100008185230476",
                "fbUserName": "Honza Sechovec",
                "amount": 100,
                "status": 0
                }
            ],
            "collectionAccount": 50000,
            "targetAmount": 7000,
            "currency": "CZK",
            "name": "Dárek pro Honzu Dárek pro Honzu",
            "description": "Honza slaví třícátiny, přispěste prosím na houpacího koně",
            "link": "http://www.zadaranabytek.cz/detail/4115-detsky-houpaci-kun-ad260.html?gclid=CNOPjLW8t74CFZclvQodPiMAxg",
            "hasImage": false
        }


## Collection reminder [/collections/{id}/notify]
### Reminds email partifipants which doesn't respond yet or which accepted participation but doesn't paid yet [POST]
+ Parameters
    + id (string) ... id of collection
    
+ Response 200


## Mobile device [/device] 
### Register new mobile device [PUT]
+ Request (application/json)

        {"gcmid": "abc123"}

+ Response 200        

