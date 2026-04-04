import re

def convert():
    with open('/media/talha/Data/development/vehicle management system/backend/backup_simple.sql', 'r', encoding='utf-8') as f:
        sql = f.read()

    # Disable constraints for import
    out = ["SET FOREIGN_KEY_CHECKS=0;"]

    # 1. Remove "PUBLIC". prefix, double quotes, and specific H2 commands
    sql = sql.replace('"PUBLIC".', '')
    sql = sql.replace('"', '')
    sql = re.sub(r'CREATE USER.*?;', '', sql)
    
    # 2. Fix data types and lowercase table names in CREATE TABLE
    sql = re.sub(r'CREATE TABLE ([A-Z_]+)\b', lambda m: f'CREATE TABLE {m.group(1).lower()}', sql)
    sql = re.sub(r'ALTER TABLE ([A-Z_]+)\b', lambda m: f'ALTER TABLE {m.group(1).lower()}', sql)
    sql = re.sub(r'INSERT INTO ([A-Z_]+)\b', lambda m: f'INSERT INTO {m.group(1).lower()}', sql)
    sql = re.sub(r'\bUUID\b(?!\s*\'|\s*\()', 'VARCHAR(36)', sql)  # Replace type UUID but not value UUID '...'
    sql = sql.replace('CHARACTER VARYING', 'VARCHAR')
    sql = re.sub(r'\bVARCHAR\b(?!\()', 'TEXT', sql) # If VARCHAR has no length, use TEXT
    sql = sql.replace('TIMESTAMP(6)', 'DATETIME(6)')
    sql = sql.replace('FLOAT(53)', 'DOUBLE')
    sql = sql.replace('NUMERIC', 'DECIMAL')
    sql = sql.replace('CACHED TABLE', 'TABLE')
    
    # 3. Replace value literals
    sql = re.sub(r"UUID '([^']+)'", r"'\1'", sql)
    sql = re.sub(r"TIMESTAMP '([^']+)'", r"'\1'", sql)
    sql = re.sub(r"DATE '([^']+)'", r"'\1'", sql)
    
    # 4. Handle U& string literals like U&'my bidget\000a'
    sql = re.sub(r"U&'my bidget\\000a'", r"'my bidget\n'", sql)
    sql = re.sub(r"U&'([^']+)'", r"'\1'", sql)

    # 5. Remove H2 specific constraints syntax NOCHECK
    sql = re.sub(r'\sNOCHECK', '', sql)

    out.append(sql)
    out.append("SET FOREIGN_KEY_CHECKS=1;\n")

    with open('/media/talha/Data/development/vehicle management system/backend/mysql_full_migration.sql', 'w', encoding='utf-8') as f:
        f.write("\n".join(out))

    print("Migration SQL generated successfully!")

if __name__ == '__main__':
    convert()
