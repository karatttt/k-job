CREATE TABLE app_info (
                              id BIGINT NOT NULL PRIMARY KEY,  -- 使用 BIGINT 类型作为 ID，并设置为主键
                              current_server VARCHAR(255),      -- currentServer 字段，设置为 VARCHAR 类型，长度为 255
                              app_name VARCHAR(255),            -- appName 字段，设置为 VARCHAR 类型，长度为 255
                              password VARCHAR(255)              -- password 字段，设置为 VARCHAR 类型，长度为 255
);