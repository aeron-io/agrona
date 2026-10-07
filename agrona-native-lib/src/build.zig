const std = @import("std");

pub fn build(b: *std.Build) void {
    const io = b.graph.io;
    const java_home_opt = b.option([]const u8, "java_home", "Path to the Java Home directory");

    // oldest libc version we support
    const libcVersion = std.SemanticVersion{.major = 2, .minor = 30, .patch = 0};

    // Define target platforms for cross-compilation
    const targets = [_]std.Target.Query{
        .{ .cpu_arch = .x86_64, .os_tag = .linux, .abi = .gnu, .cpu_model = .baseline, .glibc_version = libcVersion },
        .{ .cpu_arch = .aarch64, .os_tag = .linux, .abi = .gnu, .cpu_model = .baseline, .glibc_version = libcVersion },
    };

    // Build for all targets
    inline for (targets) |target_query| {
        const target = b.resolveTargetQuery(target_query);

        const lib = b.addLibrary(.{
            .name = "agrona",
            .linkage = .dynamic,
            .root_module = b.createModule(.{
                .target = target,
                .optimize = .ReleaseSmall,
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

        // Generate target-specific binary names
        const target_output = b.fmt(
            "libagrona-{s}-{s}.so",
            .{
                @tagName(target.result.cpu.arch),
                @tagName(target.result.os.tag),
            },
        );

        // Install with target-specific name
        const install_step = b.addInstallArtifact(lib, .{
            .dest_sub_path = target_output,
        });
        b.getInstallStep().dependOn(&install_step.step);
    }
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
