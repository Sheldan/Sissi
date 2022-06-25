from datetime import timedelta

from sqlalchemy.sql import text

from dto import LegacyWarning


def import_warnings(server_id, warnings, connection):
    user_ids = [warn.user_id for warn in warnings]
    user_ids.extend([warn.author_id for warn in warnings])
    create_users(server_id, user_ids, connection)
    channel_ids = [warn.mod_log_channel_id for warn in warnings if warn.mod_log_channel_id is not None]
    create_channels(server_id, channel_ids, connection)
    warning_id = 0
    mute_id = 0
    for warning in warnings:
        if warning.level == 5 or warning.level == 4:
            import_ban(server_id, warning, connection)
        if warning.level == 1:
            warning_id += 1
            import_warning(server_id, warning, connection, warning_id)
        if warning.level == 2:
            mute_id += 1
            import_mute(server_id, warning, connection, mute_id)
        if warning.level == 3:
            import_kick(server_id, warning, connection)
    connection.execute(f"INSERT INTO counter(counter, server_id, counter_key) VALUES ({warning_id}, {server_id}, 'WARNINGS')")
    connection.execute(f"INSERT INTO counter(counter, server_id, counter_key) VALUES ({mute_id}, {server_id}, 'MUTES')")


def import_mute(server_id, warning: LegacyWarning, connection, mute_id):
    statement = text("""INSERT INTO infraction(server_id, infraction_user_in_server_id, infraction_creator_user_in_server_id, 
                                                description, type, created, points, decayed) 
                            VALUES(:server_id, 
                            (select user_in_server_id from user_in_server where user_id = :muted_user_id and server_id = :server_id), 
                            (select user_in_server_id from user_in_server where user_id = :author_id and server_id = :server_id), 
                            :text, 'mute', :created, :points, false) returning id""")
    infraction_id = connection.execute(statement, {'server_id': server_id, 'author_id': warning.author_id, 'muted_user_id': warning.user_id,
                                                   'text': warning.reason if warning.reason is not None else 'No reason provided', 'created': warning.date, 'points': 20}).fetchone()[0]

    statement = text("""INSERT INTO infraction_parameter(key, value, infraction_id, created) 
                            VALUES(:key, :value, :infraction_id, :created)""")
    if warning.duration is not None:
        duration = str(int(warning.duration / 60)) + 'm' if warning.duration < 60 * 60 else str(int(warning.duration / 60 / 60)) + 'h'
        duration = duration if warning.duration < 60 * 60 * 24 else str(int(warning.duration / 60 / 60 / 24)) + 'd'
    else:
        duration = '0s'
    connection.execute(statement, {'key': 'DURATION', 'value': duration, 'infraction_id': infraction_id, 'created': warning.date})

    print(f'Creating infraction for mute for {warning.user_id}')
    statement = text("""INSERT INTO mute(id, server_id, muted_user_in_server_id, muting_user_in_server_id, 
                                                    reason, mute_date, target_date, mute_ended, created, infraction_id) 
                                VALUES(:mute_id, :server_id, 
                                (select user_in_server_id from user_in_server where user_id = :warned_user_id and server_id = :server_id), 
                                (select user_in_server_id from user_in_server where user_id = :author_id and server_id = :server_id), 
                                :text, :created, :target_date, true, :created, :infraction_id)""")
    connection.execute(statement, {'mute_id': mute_id, 'server_id': server_id, 'author_id': warning.author_id, 'warned_user_id': warning.user_id,
                                   'text': warning.reason if warning.reason is not None else 'No reason provided', 'created': warning.date, 'infraction_id': infraction_id,
                                   'target_date': warning.date + timedelta(seconds=warning.duration)})
    print(f'Creating warning for {warning.user_id}')

def import_warning(server_id, warning: LegacyWarning, connection, warning_id):
    statement = text("""INSERT INTO infraction(server_id, infraction_user_in_server_id, infraction_creator_user_in_server_id, 
                                                description, type, created, points, decayed) 
                            VALUES(:server_id, 
                            (select user_in_server_id from user_in_server where user_id = :warned_user_id and server_id = :server_id), 
                            (select user_in_server_id from user_in_server where user_id = :author_id and server_id = :server_id), 
                            :text, 'warn', :created, :points, false) returning id""")
    infraction_id = connection.execute(statement, {'server_id': server_id, 'author_id': warning.author_id, 'warned_user_id': warning.user_id,
                                   'text': warning.reason if warning.reason is not None else 'No reason provided', 'created': warning.date, 'points': 10}).fetchone()[0]
    print(f'Creating infraction for warning for {warning.user_id}')
    statement = text("""INSERT INTO warning(id, server_id, warned_user_in_server_id, warning_user_in_server_id, 
                                                reason, warn_date, created, decayed, infraction_id) 
                            VALUES(:warn_id, :server_id, 
                            (select user_in_server_id from user_in_server where user_id = :warned_user_id and server_id = :server_id), 
                            (select user_in_server_id from user_in_server where user_id = :author_id and server_id = :server_id), 
                            :text, :created,  :created, false, :infraction_id)""")
    connection.execute(statement, {'warn_id': warning_id, 'server_id': server_id, 'author_id': warning.author_id, 'warned_user_id': warning.user_id,
                                   'text': warning.reason if warning.reason is not None else 'No reason provided', 'created': warning.date, 'infraction_id': infraction_id})
    print(f'Creating warning for {warning.user_id}')


def import_ban(server_id, warning: LegacyWarning, connection):
    statement = text("""INSERT INTO infraction(server_id, infraction_user_in_server_id, infraction_creator_user_in_server_id, 
                                                description, type, created, points, decayed) 
                            VALUES(:server_id, 
                            (select user_in_server_id from user_in_server where user_id = :banned_user_id and server_id = :server_id), 
                            (select user_in_server_id from user_in_server where user_id = :author_id and server_id = :server_id), 
                            :text, 'ban', :created, :points, false)""")
    connection.execute(statement, {'server_id': server_id, 'author_id': warning.author_id, 'banned_user_id': warning.user_id,
                            'text': warning.reason if warning.reason is not None else 'No reason provided', 'created': warning.date, 'points': 150})
    print(f'Creating ban for {warning.user_id}')


def import_kick(server_id, warning: LegacyWarning, connection):
    statement = text("""INSERT INTO infraction(server_id, infraction_user_in_server_id, infraction_creator_user_in_server_id, 
                                                description, type, created, points, decayed) 
                            VALUES(:server_id, 
                            (select user_in_server_id from user_in_server where user_id = :kicked_user_id and server_id = :server_id), 
                            (select user_in_server_id from user_in_server where user_id = :author_id and server_id = :server_id), 
                            :text, 'kick', :created, :points, false)""")
    connection.execute(statement, {'server_id': server_id, 'author_id': warning.author_id, 'kicked_user_id': warning.user_id,
                                   'text': warning.reason if warning.reason is not None else 'No reason provided', 'created': warning.date, 'points': 50})
    print(f'Creating kick for {warning.user_id}')


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


def create_channels(server_id: int, channel_ids, con):
    channels_to_create = {}
    for channel_id in channel_ids:
        if not does_channel_exist(channel_id, con):
            channels_to_create[channel_id] = 1
    for channel_id in channels_to_create:
        create_channel(channel_id, server_id, con)

def does_channel_exist(channel_id, con):
    statement = text("""SELECT count(1) FROM channel where id = :id""")
    return con.execute(statement, {'id': channel_id}).fetchone()[0] == 1

def create_channel(channel_id, server_id, con):
    statement = text("""INSERT INTO channel(id, server_id, type, deleted) VALUES(:id, :server_id, 'TEXT', false)""")
    print(f'Creating channel {channel_id}')
    con.execute(statement, {'id': channel_id, 'server_id': server_id})


def create_user_in_server(user_id, server_id, con):
    statement = text("""INSERT INTO user_in_server(server_id, user_id) VALUES(:server_id, :user_id) returning user_in_server_id""")
    return con.execute(statement, {'user_id': user_id, 'server_id': server_id}).fetchone()[0]