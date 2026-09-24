const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});
    const java_home_opt = b.option([]const u8, "java_home", "Path to the Java Home directory");
    const io = b.graph.io;

    const lib = b.addLibrary(.{
        .name = "agrona-native-lib",
        .linkage = .dynamic,
        .root_module = b.createModule(.{
            .target = target,
            .optimize = optimize,
            .link_libc = true,
        }),
    });

    lib.root_module.addIncludePath(b.path("main/headers"));

    var resolved_java_home: ?[]const u8 = java_home_opt;
    if (resolved_java_home == null) {
        resolved_java_home = b.graph.environ_map.get("JAVA_HOME");
    }

    if (resolved_java_home) |java_home| {
        const include = b.pathJoin(&.{ java_home, "include" });
        lib.root_module.addIncludePath(.{ .cwd_relative = include });

        const target_dir: ?[]const u8 = switch (target.result.os.tag) {
            .windows => "win32",
            .linux => "linux",
            .macos => "darwin",
            else => null,
        };

        if (target_dir) |dir_name| {
            // When cross-compiling the host JDK may not ship the target's jni_md.h; its own is equivalent on 64-bit.
            if (firstExisting(b, include, &.{ dir_name, "linux", "darwin", "win32" })) |platform_include| {
                lib.root_module.addIncludePath(.{ .cwd_relative = platform_include });
            }
        }
    }

    const c_flags = &[_][]const u8{ "-std=c11", "-Wall", "-Wextra" };

    var dir = b.build_root.handle.openDir(io, ".", .{ .iterate = true }) catch |err| {
        std.debug.print("Failed to open current directory: {}\n", .{err});
        return;
    };
    defer dir.close(io);

    var walker = dir.walk(b.allocator) catch return;
    defer walker.deinit();

    // Compile all C code
    while (walker.next(io) catch null) |entry| {
        if (entry.kind == .file and std.mem.endsWith(u8, entry.path, ".c")) {
            lib.root_module.addCSourceFile(.{
                .file = b.path(b.dupe(entry.path)),
                .flags = c_flags,
            });
        }
    }

    b.installArtifact(lib);
}

fn firstExisting(b: *std.Build, base: []const u8, names: []const []const u8) ?[]const u8 {
    for (names) |name| {
        const path = b.pathJoin(&.{ base, name });
        if (std.Io.Dir.accessAbsolute(b.graph.io, path, .{})) {
            return path;
        } else |_| {}
    }
    return null;
}
