# Installation Guide

This guide will walk you through installing and setting up NoNetheritePlus on your Minecraft server.

## 📋 Prerequisites

Before installing NoNetheritePlus, ensure your server meets these requirements:

### System Requirements
- **Minecraft Version**: 1.21 or higher
- **Java Version**: Java 21 or higher
- **Server Software**: Paper or Spigot
- **RAM**: At least 2GB available (recommended 4GB+)

### Recommended Server Setup
```bash
# Example server startup script
java -Xmx4G -Xms4G -jar paper-1.21.jar nogui
```

## 📥 Download

### Option 1: Official Releases (Recommended)
1. Visit the [GitHub Releases](https://github.com/codingsushi79/nonetheriteplus/releases) page
2. Download the latest `NoNetheritePlus-X.X.X.jar` file
3. Save it to your server's `plugins/` directory

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/codingsushi79/nonetheriteplus.git
cd nonetheriteplus

# Build the plugin
gradle clean build

# Copy to plugins folder
cp build/libs/NoNetheritePlus-*.jar /path/to/server/plugins/
```

## 🛠️ Installation Steps

### Step 1: Prepare Your Server
1. **Stop your server** if it's currently running
2. **Navigate to your plugins directory**:
   ```bash
   cd /path/to/your/server/plugins/
   ```

### Step 2: Install the Plugin
1. **Place the JAR file** in your `plugins/` directory:
   ```bash
   # Example
   cp ~/downloads/NoNetheritePlus-1.4.0.jar ./plugins/
   ```

2. **Verify the file** is in the correct location:
   ```bash
   ls -la plugins/NoNetheritePlus-*.jar
   ```

### Step 3: Start the Server
1. **Start your server** normally:
   ```bash
   # From your server directory
   java -Xmx4G -Xms4G -jar paper-1.21.jar nogui
   ```

2. **Check the console** for successful loading:
   ```
   [INFO] [NoNetheritePlus] Enabling NoNetheritePlus v1.4.0
   [INFO] [NoNetheritePlus] NoNetheritePlus plugin enabled!
   ```

### Step 4: Verify Installation
1. **Join your server** as an operator (OP)
2. **Run the plugin command**:
   ```
   /nonetherite
   ```
   You should see plugin information displayed.

## ⚙️ Initial Configuration

### Automatic Setup
NoNetheritePlus automatically creates:
- `plugins/NoNetheritePlus/config.yml` - Main configuration file
- `plugins/NoNetheritePlus/statistics.yml` - Usage statistics (if enabled)

### Basic Configuration
After first startup, edit `config.yml` to customize behavior:

```yaml
# Quick start configuration
disable_netherite_helmet: true
disable_netherite_chestplate: true
disable_netherite_leggings: true
disable_netherite_boots: true
refund_materials: true
```

## 🔄 Updating the Plugin

### Safe Update Process
1. **Download the new version**
2. **Stop your server**
3. **Replace the old JAR file**:
   ```bash
   # Backup old version (optional)
   mv plugins/NoNetheritePlus-1.3.0.jar plugins/NoNetheritePlus-1.3.0.jar.backup

   # Install new version
   cp NoNetheritePlus-1.4.0.jar plugins/
   ```
4. **Start your server**
5. **Run configuration reload** (optional):
   ```
   /nonetherite reload
   ```

### Configuration Migration
Most configuration options are backward compatible. Check the changelog for any breaking changes.

## 🐛 Troubleshooting Installation

### Plugin Doesn't Load
**Symptoms**: No plugin messages in console, commands don't work

**Solutions**:
1. Check Java version: `java -version` (must be 21+)
2. Verify JAR file integrity
3. Check server logs for errors
4. Ensure correct server software (Paper/Spigot)

### Configuration Errors
**Symptoms**: Plugin loads but some features don't work

**Solutions**:
1. Check `config.yml` syntax with a YAML validator
2. Look for error messages in console
3. Reset to default config by deleting `config.yml` and restarting

### Permission Issues
**Symptoms**: Commands show "no permission" errors

**Solutions**:
1. Ensure you're an operator: `op yourusername`
2. Check permission plugin configuration
3. Verify permission nodes in `plugin.yml`

## 🔧 Advanced Installation

### Multi-Server Setup
For networks with multiple servers:

1. **Shared Configuration**: Use symbolic links or shared storage
2. **Server-Specific Settings**: Configure different worlds per server
3. **Centralized Management**: Use configuration presets

### Performance Tuning
For high-traffic servers:

```yaml
# Optimize for performance
statistics:
  enabled: true
  log_to_file: false  # Reduce disk I/O

debug:
  enabled: false  # Disable in production
```

## 📞 Support

If you encounter issues:

1. **Check the console** for error messages
2. **Review server logs** in `logs/latest.log`
3. **Test with default config** (rename `config.yml` to `config.yml.backup`)
4. **Create an issue** on [GitHub](https://github.com/codingsushi79/nonetheriteplus/issues)

## ✅ Next Steps

Once installed, proceed to:
- **[Configuration Guide](Configuration.md)** - Customize plugin behavior
- **[Commands Reference](Commands.md)** - Learn available commands
- **[Troubleshooting](Troubleshooting.md)** - Solve common issues

---

*Installation verified for Paper 1.21+, Spigot 1.21+, Java 21+*
