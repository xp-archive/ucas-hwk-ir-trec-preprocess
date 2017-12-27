import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xp
 */
public class AppMain {

    private static final NxmlParser parser = new NxmlParser();

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final AtomicInteger totalCounter = new AtomicInteger(0);

    private static final AtomicInteger doneCounter = new AtomicInteger(0);

    private static final AtomicInteger emptyBodyCounter = new AtomicInteger(0);

    private static void handleOne(Path in, Path out) throws IOException {
        if (Files.exists(out) && Files.size(out) > 0) {
            // 跳过已完成的任务
            return;
        }
        Result result = parser.parse(() -> {
            try {
                return Files.newBufferedReader(in, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        doneCounter.incrementAndGet();
        if (result.body == null || result.body.trim().isEmpty()) {
            emptyBodyCounter.incrementAndGet();
        }
        BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8);
        mapper.writeValue(writer, result);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(String.format("run with %s", Arrays.toString(args)));

        int threadNum = Integer.parseInt(args[0]);
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);

        Path sourceDir = Paths.get(args[1]);
        if (!Files.exists(sourceDir)) {
            throw new Exception(String.format("source %s is not exists.", sourceDir.toString()));
        }

        Path outDir = Paths.get(args[2]);
//        if (Files.notExists(outDir)) {
//            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-rw-rw-");
//            Files.createDirectories(outDir, PosixFilePermissions.asFileAttribute(permissions));
//        }
        if (!Files.exists(outDir)) {
            throw new Exception(String.format("target %s is not exists.", outDir.toString()));
        }
        if (!Files.isDirectory(outDir)) {
            throw new Exception(String.format("target %s is not a directory.", outDir.toString()));
        }

        long time1 = System.currentTimeMillis();

        try {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".nxml")) {
                        totalCounter.incrementAndGet();
                        executor.submit(() -> {
                            try {
                                handleOne(file, outDir.resolve(file.getFileName() + ".json"));
                            } catch (Exception e) {
                                System.err.println(String.format("handle %s", file.toString()));
                                e.printStackTrace(System.err);
                            }
                        });
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        executor.shutdown();

        int total = totalCounter.get();
        System.out.println(String.format("total %d, awaiting..", total));

        int mins = 0;
        while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            int done = doneCounter.get();
            double percent = 100.0 * done / total;
            System.out.println(String.format("%f%%, total %d, done %d, awaiting %d mins..",
                    percent, total, done,
                    ++mins
            ));
        }

        long time2 = System.currentTimeMillis();

        System.out.println(String.format("total %d, done %d. used %ds. empty-body %d.",
                total, doneCounter.get(),
                (time2 - time1) / 1000,
                emptyBodyCounter.get()
        ));
    }
}
