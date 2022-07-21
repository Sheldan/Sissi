from sqlalchemy.sql import text

def import_commands(server_id, commands, connection):
    user_ids = [command.author_id for command in commands]
    create_users(server_id, user_ids, connection)
    command_id = 2
    for command in commands:
        statement = text("""INSERT INTO custom_command(server_id, id, creator_user_in_server_id, additional_message, name) 
                            VALUES(:server_id, :id,
                            (select user_in_server_id from user_in_server where user_id = :author_id and server_id = :server_id), 
                            :additional_message, :name)""")
        connection.execute(statement,
                           {'server_id': server_id, 'author_id': command.author_id,
                            'name': command.command, 'additional_message': command.response,
                            'id': command_id})
        command_id += 1
        print(f'Creating command for {command.command}')


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