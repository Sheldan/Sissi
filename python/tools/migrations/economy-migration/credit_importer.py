from sqlalchemy.sql import text


def import_credits(server_id, credits, connection):
    user_ids = [credit.user_id for credit in credits]
    create_users(server_id, user_ids, connection)
    for credit in credits:
        statement = text("""INSERT INTO economy_user(server_id, last_pay_day, last_slots, 
                                            id, credits) 
                        VALUES(:server_id, now(), now(),
                        (select user_in_server_id from user_in_server where user_id = :user_id and server_id = :server_id), :credits)""")
        connection.execute(statement, {'server_id': server_id, 'user_id': credit.user_id, 'credits': credit.credits})
        print(f'Creating economy user for {credit.user_id}')


def create_users(server_id: int, user_ids, con):
    created_users = {}
    for user_id in user_ids:
        if not does_user_exist(user_id, con) and user_id not in created_users:
            create_user(user_id, con)
            create_user_in_server(user_id, server_id, con)
            created_users[user_id] = 1


def does_user_exist(user_id, con):
    statement = text("""SELECT count(1) FROM auser where id = :id""")
    return con.execute(statement, {'id': user_id}).fetchone()[0] == 1


def create_user(user_id, con):
    statement = text("""INSERT INTO auser(id) VALUES(:id)""")
    print(f'Creating user {user_id}')
    con.execute(statement, {'id': user_id})

def create_user_in_server(user_id, server_id, con):
    statement = text("""INSERT INTO user_in_server(server_id, user_id) VALUES(:server_id, :user_id) returning user_in_server_id""")
    return con.execute(statement, {'user_id': user_id, 'server_id': server_id}).fetchone()[0]