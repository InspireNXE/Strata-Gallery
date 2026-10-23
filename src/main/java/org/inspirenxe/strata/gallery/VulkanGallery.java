/*
 * Copyright (c) InspireNXE <https://github.com/InspireNXE>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inspirenxe.strata.gallery;

import org.inspirenxe.strata.gallery.theme.GalleryLook;
import org.inspirenxe.strata.gallery.theme.ThemeChoice;
import org.inspirenxe.strata.spi.ScreenStack;
import org.inspirenxe.strata.vulkan.PixelRect;
import org.inspirenxe.strata.vulkan.StrataVulkan;
import org.inspirenxe.strata.vulkan.VulkanFrame;
import org.inspirenxe.strata.vulkan.VulkanPlatform;
import org.inspirenxe.strata.vulkan.VulkanRenderer;
import org.inspirenxe.strata.vulkan.VulkanTarget;
import org.inspirenxe.strata.vulkan.glfw.StrataGlfw;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkImageCopy;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceDynamicRenderingFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkPresentRegionKHR;
import org.lwjgl.vulkan.VkPresentRegionsKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRectLayerKHR;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_NO_API;
import static org.lwjgl.glfw.GLFW.GLFW_SCALE_TO_MONITOR;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorWorkarea;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwPostEmptyEvent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWaitEventsTimeout;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.KHRDynamicRendering.VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRIncrementalPresent.VK_KHR_INCREMENTAL_PRESENT_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRIncrementalPresent.VK_STRUCTURE_TYPE_PRESENT_REGIONS_KHR;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_LOAD;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCmdClearAttachments;
import static org.lwjgl.vulkan.VK10.vkCmdCopyImage;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyFence;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkResetCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;
import static org.lwjgl.vulkan.VK13.VK_API_VERSION_1_3;
import static org.lwjgl.vulkan.VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DYNAMIC_RENDERING_FEATURES;
import static org.lwjgl.vulkan.VK13.VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO;
import static org.lwjgl.vulkan.VK13.VK_STRUCTURE_TYPE_RENDERING_INFO;

/**
 * The gallery in a GLFW window, drawn with Vulkan: the host a Vulkan application gives Strata, written out in full.
 * It makes an instance, a surface, a device and a swapchain, and keeps an image of the last frame. Each frame the
 * Strata view prepares tells what changed; the host clears those rectangles to the Swing panel's background, lets the
 * view draw over them with dynamic rendering, copies the kept image to the swapchain image and presents it, naming the
 * changed rectangles when the device has {@code VK_KHR_incremental_present}. A frame where nothing changed is not
 * presented. Frames are drawn when the screens need one or a timed change is due, never sooner than the frame rate
 * allows, and the window otherwise waits for events.
 *
 * <p>Changing look makes a new view, with a platform of the new look's fonts, on the same renderer.</p>
 */
final class VulkanGallery extends GalleryHost {
    private static final int FRAMES_IN_FLIGHT = 2;
    /** The longest the loop sleeps between frames while nothing asks for one, in nanoseconds. */
    private static final long IDLE_NANOS = 500_000_000L;
    /** The Swing panel's background, so both renderers show the gallery on the same ground. */
    private static final int BACKGROUND = 0xFF1E1F22;

    private final Queue<Runnable> tasks;
    private final Thread thread;
    private long window;
    private VkInstance instance;
    private long surface;
    private VkDevice device;
    private VkQueue queue;
    private int queueFamily;
    private boolean core13;
    private int format;
    private long swapchain;
    private int width;
    private int height;
    private long[] images;
    private long[] views;
    private long[] renderFinished;
    private long commandPool;
    private VkCommandBuffer[] commands;
    private long[] imageAvailable;
    private long[] inFlight;
    private int frame;
    private boolean stale;
    private boolean incremental;
    private long kept;
    private long keptMemory;
    private long keptView;
    private boolean keptLoaded;
    private VulkanRenderer renderer;
    private StrataVulkan strata;
    private StrataGlfw glfw;
    private float zoom;

    VulkanGallery(final List<ThemeChoice> themes, final GalleryLook look, final int frameRate) {
        super(themes, look, frameRate);
        this.tasks = new ConcurrentLinkedQueue<>();
        this.thread = Thread.currentThread();
        this.zoom = Gallery.SCALE;
    }

    /**
     * Opens the window and runs it until it closes. Call it on the main thread, which GLFW needs.
     */
    void run() {
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW cannot start");
        }
        try {
            this.openWindow();
            this.createDevice();
            this.createSwapchain();
            this.createFrames();
            this.renderer = new VulkanRenderer(this.device, this.queue, this.queueFamily,
                    VulkanTarget.dynamic(this.format), FRAMES_IN_FLIGHT);
            this.openView(this.look);
            this.strata.screens().push(this.screen(this.pages.getFirst()));
            glfwShowWindow(this.window);
            this.loop();
        } finally {
            this.destroy();
            glfwTerminate();
        }
    }

    @Override
    ScreenStack screens() {
        return this.strata.screens();
    }

    @Override
    void later(final Runnable task) {
        this.tasks.add(task);
        glfwPostEmptyEvent();
    }

    @Override
    float scale() {
        return this.zoom;
    }

    @Override
    void setScale(final float scale) {
        this.zoom = scale;
        this.glfw.setZoom(scale);
    }

    @Override
    void showLook(final GalleryLook look) {
        // The old view's frames must be done before it lets go of its buffers
        vkDeviceWaitIdle(this.device);
        this.glfw.close();
        this.strata.close();
        this.openView(look);
    }

    /**
     * Makes a view with a platform of the look's fonts, and connects it to the window.
     */
    private void openView(final GalleryLook look) {
        final VulkanPlatform platform = StrataGlfw.platform(this.window, look.vulkanMetrics())
                .uiExecutor(this::later)
                .uiThreadCheck(() -> Thread.currentThread() == this.thread)
                .build();
        this.strata = new StrataVulkan(platform, this.renderer, look.theme(), 1f, look.vulkanSprites());
        this.strata.screens().setFrameRate(this.frameRate);
        // Strata.screens() opens on this view. The gallery pushes its own screens, so they follow theme changes
        this.strata.setDefault();
        // Any thread may ask for a frame; an empty event wakes the loop
        this.strata.screens().setFrameRequester(org.lwjgl.glfw.GLFW::glfwPostEmptyEvent);
        this.glfw = StrataGlfw.attach(this.window, this.strata, this.zoom);
        // The close button asks the screens first
        this.glfw.manageWindow(Gallery.title());
    }

    private void loop() {
        long next = 0L;
        long last = Long.MIN_VALUE / 2;
        while (!glfwWindowShouldClose(this.window)) {
            Runnable task;
            while ((task = this.tasks.poll()) != null) {
                task.run();
            }
            if (this.stale || this.resized()) {
                this.recreateSwapchain();
            }
            final ScreenStack screens = this.strata.screens();
            final long interval = screens.frameIntervalNanos();
            final long now = System.nanoTime();
            // A needed frame waits until one interval has passed since the last one began
            final boolean due = screens.needsFrame() && now >= last + interval || now >= next;
            if (due && this.width > 0 && this.height > 0) {
                last = now;
                this.drawFrame();
                next = screens.nextFrameNanos();
            }
            long wake = next == Long.MAX_VALUE ? System.nanoTime() + IDLE_NANOS : next;
            if (screens.needsFrame()) {
                wake = Math.min(wake, last + interval);
            }
            final long wait = Math.min(IDLE_NANOS, wake - System.nanoTime());
            if (wait <= 0L || !this.tasks.isEmpty() || this.stale) {
                glfwPollEvents();
            } else {
                glfwWaitEventsTimeout(wait / 1e9);
            }
        }
    }

    /**
     * Returns whether the framebuffer's size no longer matches the swapchain's, after a resize or a restore.
     */
    private boolean resized() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer width = stack.mallocInt(1);
            final IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(this.window, width, height);
            return width.get(0) != this.width || height.get(0) != this.height;
        }
    }

    private void drawFrame() {
        final VulkanFrame prepared = this.strata.prepare();
        if (prepared.isEmpty()) {
            // Nothing changed, so the window keeps what it shows
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final long fence = this.inFlight[this.frame];
            check(vkWaitForFences(this.device, stack.longs(fence), true, Long.MAX_VALUE), "wait for a frame");
            final IntBuffer index = stack.mallocInt(1);
            final int acquired = vkAcquireNextImageKHR(this.device, this.swapchain, Long.MAX_VALUE,
                    this.imageAvailable[this.frame], 0L, index);
            if (acquired == VK_ERROR_OUT_OF_DATE_KHR) {
                // The prepared frame is lost with the swapchain; the next one is drawn whole
                this.stale = true;
                return;
            }
            if (acquired != VK_SUBOPTIMAL_KHR) {
                check(acquired, "acquire a swapchain image");
            }
            final int image = index.get(0);
            check(vkResetFences(this.device, stack.longs(fence)), "reset a fence");
            final VkCommandBuffer commands = this.commands[this.frame];
            vkResetCommandBuffer(commands, 0);
            check(vkBeginCommandBuffer(commands, VkCommandBufferBeginInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)), "begin a frame");
            // The kept image holds the last frame, unless it is new; the first frame damages all of it
            this.barrier(commands, stack, this.kept,
                    this.keptLoaded ? VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL : VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_ACCESS_TRANSFER_READ_BIT,
                    VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            final VkRenderingAttachmentInfo.Buffer color = VkRenderingAttachmentInfo.calloc(1, stack)
                    .sType(VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO)
                    .imageView(this.keptView)
                    .imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_LOAD)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            final PixelRect area = prepared.bounds();
            final VkRenderingInfo rendering = VkRenderingInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDERING_INFO)
                    .layerCount(1)
                    .pColorAttachments(color);
            rendering.renderArea().offset().set(area.x(), area.y());
            rendering.renderArea().extent().set(area.width(), area.height());
            if (this.core13) {
                VK13.vkCmdBeginRendering(commands, rendering);
            } else {
                KHRDynamicRendering.vkCmdBeginRenderingKHR(commands, rendering);
            }
            this.clearDamage(commands, stack, prepared.damage());
            prepared.record(commands);
            if (this.core13) {
                VK13.vkCmdEndRendering(commands);
            } else {
                KHRDynamicRendering.vkCmdEndRenderingKHR(commands);
            }
            this.barrier(commands, stack, this.kept, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
                    VK_ACCESS_TRANSFER_READ_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT);
            // Vulkan does not promise a swapchain image keeps its pixels between presents, so all of the kept image is copied
            this.barrier(commands, stack, this.images[image], VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 0, VK_ACCESS_TRANSFER_WRITE_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
            final VkImageCopy.Buffer copy = VkImageCopy.calloc(1, stack);
            copy.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).layerCount(1);
            copy.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).layerCount(1);
            copy.extent().set(this.width, this.height, 1);
            vkCmdCopyImage(commands, this.kept, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, this.images[image],
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copy);
            this.barrier(commands, stack, this.images[image], VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK_ACCESS_TRANSFER_WRITE_BIT, 0,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT);
            check(vkEndCommandBuffer(commands), "end a frame");
            this.keptLoaded = true;

            final VkSubmitInfo submit = VkSubmitInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .waitSemaphoreCount(1)
                    .pWaitSemaphores(stack.longs(this.imageAvailable[this.frame]))
                    .pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_TRANSFER_BIT))
                    .pCommandBuffers(stack.pointers(commands))
                    .pSignalSemaphores(stack.longs(this.renderFinished[image]));
            check(vkQueueSubmit(this.queue, submit, fence), "submit a frame");
            final VkPresentInfoKHR present = VkPresentInfoKHR.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(stack.longs(this.renderFinished[image]))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(this.swapchain))
                    .pImageIndices(stack.ints(image));
            if (this.incremental) {
                // Only the damage changed, so the compositor need not look at the rest
                final List<PixelRect> damage = prepared.damage();
                final VkRectLayerKHR.Buffer rects = VkRectLayerKHR.calloc(damage.size(), stack);
                for (int i = 0; i < damage.size(); i++) {
                    final PixelRect rect = damage.get(i);
                    rects.get(i).offset().set(rect.x(), rect.y());
                    rects.get(i).extent().set(rect.width(), rect.height());
                    rects.get(i).layer(0);
                }
                final VkPresentRegionKHR.Buffer regions = VkPresentRegionKHR.calloc(1, stack)
                        .rectangleCount(damage.size())
                        .pRectangles(rects);
                present.pNext(VkPresentRegionsKHR.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_PRESENT_REGIONS_KHR)
                        .swapchainCount(1)
                        .pRegions(regions)
                        .address());
            }
            final int presented = vkQueuePresentKHR(this.queue, present);
            if (presented == VK_ERROR_OUT_OF_DATE_KHR || presented == VK_SUBOPTIMAL_KHR) {
                this.stale = true;
            } else {
                check(presented, "present a frame");
            }
            this.frame = (this.frame + 1) % FRAMES_IN_FLIGHT;
        }
    }

    /**
     * Clears the damage rectangles of the kept image to the background, so the view draws them as on a new image.
     */
    private void clearDamage(final VkCommandBuffer commands, final MemoryStack stack, final List<PixelRect> damage) {
        final VkClearAttachment.Buffer attachment = VkClearAttachment.calloc(1, stack)
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .colorAttachment(0);
        attachment.clearValue().color().float32(0, ((BACKGROUND >> 16) & 0xFF) / 255f)
                .float32(1, ((BACKGROUND >> 8) & 0xFF) / 255f).float32(2, (BACKGROUND & 0xFF) / 255f).float32(3, 1f);
        final VkClearRect.Buffer rects = VkClearRect.calloc(damage.size(), stack);
        for (int i = 0; i < damage.size(); i++) {
            final PixelRect rect = damage.get(i);
            rects.get(i).baseArrayLayer(0).layerCount(1);
            rects.get(i).rect().offset().set(rect.x(), rect.y());
            rects.get(i).rect().extent().set(rect.width(), rect.height());
        }
        vkCmdClearAttachments(commands, attachment, rects);
    }

    /**
     * Makes the image that keeps the last frame, the size and format of the swapchain's images.
     */
    private void createKept() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final VkImageCreateInfo info = VkImageCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(VK_IMAGE_TYPE_2D)
                    .format(this.format)
                    .mipLevels(1)
                    .arrayLayers(1)
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .tiling(VK_IMAGE_TILING_OPTIMAL)
                    .usage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            info.extent().set(this.width, this.height, 1);
            final LongBuffer handle = stack.mallocLong(1);
            check(vkCreateImage(this.device, info, null, handle), "create the kept image");
            this.kept = handle.get(0);
            final VkMemoryRequirements requirements = VkMemoryRequirements.malloc(stack);
            vkGetImageMemoryRequirements(this.device, this.kept, requirements);
            final VkPhysicalDeviceMemoryProperties memory = VkPhysicalDeviceMemoryProperties.malloc(stack);
            vkGetPhysicalDeviceMemoryProperties(this.device.getPhysicalDevice(), memory);
            int type = -1;
            for (int i = 0; i < memory.memoryTypeCount() && type < 0; i++) {
                if ((requirements.memoryTypeBits() & (1 << i)) != 0
                        && (memory.memoryTypes(i).propertyFlags() & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) != 0) {
                    type = i;
                }
            }
            if (type < 0) {
                throw new IllegalStateException("No device memory holds the kept image");
            }
            check(vkAllocateMemory(this.device, VkMemoryAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(requirements.size())
                    .memoryTypeIndex(type), null, handle), "allocate the kept image");
            this.keptMemory = handle.get(0);
            check(vkBindImageMemory(this.device, this.kept, this.keptMemory, 0L), "bind the kept image");
            final VkImageViewCreateInfo view = VkImageViewCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(this.kept)
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(this.format);
            view.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).levelCount(1).layerCount(1);
            check(vkCreateImageView(this.device, view, null, handle), "create the kept image's view");
            this.keptView = handle.get(0);
            this.keptLoaded = false;
        }
    }

    private void destroyKept() {
        if (this.kept != 0L) {
            vkDestroyImageView(this.device, this.keptView, null);
            vkDestroyImage(this.device, this.kept, null);
            vkFreeMemory(this.device, this.keptMemory, null);
            this.kept = 0L;
        }
    }

    private void openWindow() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        // The size is in the display's own units, as Swing's is
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);
        this.window = glfwCreateWindow(Gallery.WIDTH, Gallery.HEIGHT, Gallery.title(), 0L, 0L);
        if (this.window == 0L) {
            throw new IllegalStateException("GLFW cannot open a window");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer x = stack.mallocInt(1);
            final IntBuffer y = stack.mallocInt(1);
            final IntBuffer areaWidth = stack.mallocInt(1);
            final IntBuffer areaHeight = stack.mallocInt(1);
            glfwGetMonitorWorkarea(glfwGetPrimaryMonitor(), x, y, areaWidth, areaHeight);
            final IntBuffer windowWidth = stack.mallocInt(1);
            final IntBuffer windowHeight = stack.mallocInt(1);
            glfwGetWindowSize(this.window, windowWidth, windowHeight);
            glfwSetWindowPos(this.window, x.get(0) + Math.max(0, (areaWidth.get(0) - windowWidth.get(0)) / 2),
                    y.get(0) + Math.max(0, (areaHeight.get(0) - windowHeight.get(0)) / 2));
        }
    }

    private void createDevice() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final PointerBuffer required = GLFWVulkan.glfwGetRequiredInstanceExtensions();
            if (required == null) {
                throw new IllegalStateException("GLFW finds no Vulkan loader for windows");
            }
            final VkApplicationInfo app = VkApplicationInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(stack.UTF8("Strata Gallery"))
                    .apiVersion(Math.max(Math.min(VK.getInstanceVersionSupported(), VK_API_VERSION_1_3),
                            VK_API_VERSION_1_2));
            final VkInstanceCreateInfo info = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(app)
                    .ppEnabledExtensionNames(required);
            final PointerBuffer handle = stack.mallocPointer(1);
            check(vkCreateInstance(info, null, handle), "create an instance");
            this.instance = new VkInstance(handle.get(0), info);
            final LongBuffer surface = stack.mallocLong(1);
            check(GLFWVulkan.glfwCreateWindowSurface(this.instance, this.window, null, surface), "create a surface");
            this.surface = surface.get(0);

            final IntBuffer count = stack.mallocInt(1);
            vkEnumeratePhysicalDevices(this.instance, count, null);
            final PointerBuffer devices = stack.mallocPointer(count.get(0));
            vkEnumeratePhysicalDevices(this.instance, count, devices);
            VkPhysicalDevice chosen = null;
            boolean discrete = false;
            for (int i = 0; i < devices.capacity(); i++) {
                final VkPhysicalDevice candidate = new VkPhysicalDevice(devices.get(i), this.instance);
                final int family = this.presentingFamily(stack, candidate);
                if (family < 0 || !VulkanGallery.hasExtension(stack, candidate, VK_KHR_SWAPCHAIN_EXTENSION_NAME)) {
                    continue;
                }
                final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
                vkGetPhysicalDeviceProperties(candidate, properties);
                if (properties.apiVersion() < VK_API_VERSION_1_3
                        && !VulkanGallery.hasExtension(stack, candidate, VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME)) {
                    continue;
                }
                final boolean isDiscrete = properties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
                if (chosen == null || isDiscrete && !discrete) {
                    chosen = candidate;
                    this.queueFamily = family;
                    discrete = isDiscrete;
                }
            }
            if (chosen == null) {
                throw new IllegalStateException("No Vulkan device can draw to this window with dynamic rendering");
            }
            final VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(chosen, properties);
            System.out.println("Vulkan: " + properties.deviceNameString());
            this.incremental = VulkanGallery.hasExtension(stack, chosen, VK_KHR_INCREMENTAL_PRESENT_EXTENSION_NAME);
            final int version = Math.min(properties.apiVersion(), app.apiVersion());
            this.core13 = version >= VK_API_VERSION_1_3;
            final VkPhysicalDeviceDynamicRenderingFeatures features = VkPhysicalDeviceDynamicRenderingFeatures
                    .calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DYNAMIC_RENDERING_FEATURES)
                    .dynamicRendering(true);
            final VkDeviceCreateInfo deviceInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pNext(features.address())
                    .pQueueCreateInfos(VkDeviceQueueCreateInfo.calloc(1, stack)
                            .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                            .queueFamilyIndex(this.queueFamily)
                            .pQueuePriorities(stack.floats(1f)))
                    .ppEnabledExtensionNames(this.deviceExtensions(stack));
            final PointerBuffer deviceHandle = stack.mallocPointer(1);
            check(vkCreateDevice(chosen, deviceInfo, null, deviceHandle), "create a device");
            this.device = new VkDevice(deviceHandle.get(0), chosen, deviceInfo, version);
            final PointerBuffer queue = stack.mallocPointer(1);
            vkGetDeviceQueue(this.device, this.queueFamily, 0, queue);
            this.queue = new VkQueue(queue.get(0), this.device);
            this.format = this.chooseFormat(stack, chosen);
        }
    }

    private PointerBuffer deviceExtensions(final MemoryStack stack) {
        final PointerBuffer names = stack.mallocPointer(3);
        names.put(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
        if (!this.core13) {
            names.put(stack.UTF8(VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME));
        }
        if (this.incremental) {
            names.put(stack.UTF8(VK_KHR_INCREMENTAL_PRESENT_EXTENSION_NAME));
        }
        return names.flip();
    }

    private int presentingFamily(final MemoryStack stack, final VkPhysicalDevice device) {
        final IntBuffer count = stack.mallocInt(1);
        vkGetPhysicalDeviceQueueFamilyProperties(device, count, null);
        final VkQueueFamilyProperties.Buffer families = VkQueueFamilyProperties.malloc(count.get(0), stack);
        vkGetPhysicalDeviceQueueFamilyProperties(device, count, families);
        final IntBuffer presents = stack.mallocInt(1);
        for (int i = 0; i < families.capacity(); i++) {
            vkGetPhysicalDeviceSurfaceSupportKHR(device, i, this.surface, presents);
            if ((families.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0 && presents.get(0) != 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Picks an 8-bit format that does not encode to sRGB, so blending happens on the stored values as Java2D's does.
     */
    private int chooseFormat(final MemoryStack stack, final VkPhysicalDevice device) {
        final IntBuffer count = stack.mallocInt(1);
        vkGetPhysicalDeviceSurfaceFormatsKHR(device, this.surface, count, null);
        final VkSurfaceFormatKHR.Buffer formats = VkSurfaceFormatKHR.malloc(count.get(0), stack);
        vkGetPhysicalDeviceSurfaceFormatsKHR(device, this.surface, count, formats);
        for (final VkSurfaceFormatKHR format : formats) {
            if ((format.format() == VK_FORMAT_B8G8R8A8_UNORM || format.format() == VK_FORMAT_R8G8B8A8_UNORM)
                    && format.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                return format.format();
            }
        }
        return formats.get(0).format();
    }

    private void createSwapchain() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
            check(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.device.getPhysicalDevice(), this.surface,
                    capabilities), "read the surface");
            final IntBuffer framebufferWidth = stack.mallocInt(1);
            final IntBuffer framebufferHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(this.window, framebufferWidth, framebufferHeight);
            if (capabilities.currentExtent().width() != -1) {
                this.width = capabilities.currentExtent().width();
                this.height = capabilities.currentExtent().height();
            } else {
                this.width = Math.clamp(framebufferWidth.get(0), capabilities.minImageExtent().width(),
                        capabilities.maxImageExtent().width());
                this.height = Math.clamp(framebufferHeight.get(0), capabilities.minImageExtent().height(),
                        capabilities.maxImageExtent().height());
            }
            if (this.width == 0 || this.height == 0) {
                // Minimized: no swapchain until the window has a size again
                this.swapchain = 0L;
                this.images = new long[0];
                this.views = new long[0];
                this.renderFinished = new long[0];
                return;
            }
            int imageCount = capabilities.minImageCount() + 1;
            if (capabilities.maxImageCount() > 0) {
                imageCount = Math.min(imageCount, capabilities.maxImageCount());
            }
            final VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(this.surface)
                    .minImageCount(imageCount)
                    .imageFormat(this.format)
                    .imageColorSpace(VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                    .imageArrayLayers(1)
                    .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT)
                    .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .preTransform(capabilities.currentTransform())
                    .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .presentMode(VK_PRESENT_MODE_FIFO_KHR)
                    .clipped(true);
            info.imageExtent().set(this.width, this.height);
            final LongBuffer swapchain = stack.mallocLong(1);
            check(vkCreateSwapchainKHR(this.device, info, null, swapchain), "create a swapchain");
            this.swapchain = swapchain.get(0);
            final IntBuffer count = stack.mallocInt(1);
            vkGetSwapchainImagesKHR(this.device, this.swapchain, count, null);
            final LongBuffer images = stack.mallocLong(count.get(0));
            vkGetSwapchainImagesKHR(this.device, this.swapchain, count, images);
            this.images = new long[count.get(0)];
            this.views = new long[count.get(0)];
            this.renderFinished = new long[count.get(0)];
            for (int i = 0; i < this.images.length; i++) {
                this.images[i] = images.get(i);
                final VkImageViewCreateInfo view = VkImageViewCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                        .image(this.images[i])
                        .viewType(VK_IMAGE_VIEW_TYPE_2D)
                        .format(this.format);
                view.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).levelCount(1).layerCount(1);
                final LongBuffer handle = stack.mallocLong(1);
                check(vkCreateImageView(this.device, view, null, handle), "create a swapchain image view");
                this.views[i] = handle.get(0);
                this.renderFinished[i] = this.semaphore(stack);
            }
        }
        this.createKept();
        if (this.strata != null) {
            this.strata.setBounds(0, 0, this.width, this.height);
            // The kept image is new
            this.strata.invalidate();
        }
    }

    private void recreateSwapchain() {
        vkDeviceWaitIdle(this.device);
        this.destroySwapchain();
        this.createSwapchain();
        this.stale = false;
    }

    private void createFrames() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final LongBuffer pool = stack.mallocLong(1);
            check(vkCreateCommandPool(this.device, VkCommandPoolCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(this.queueFamily), null, pool), "create a command pool");
            this.commandPool = pool.get(0);
            final PointerBuffer buffers = stack.mallocPointer(FRAMES_IN_FLIGHT);
            check(vkAllocateCommandBuffers(this.device, VkCommandBufferAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(this.commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(FRAMES_IN_FLIGHT), buffers), "allocate command buffers");
            this.commands = new VkCommandBuffer[FRAMES_IN_FLIGHT];
            this.imageAvailable = new long[FRAMES_IN_FLIGHT];
            this.inFlight = new long[FRAMES_IN_FLIGHT];
            for (int i = 0; i < FRAMES_IN_FLIGHT; i++) {
                this.commands[i] = new VkCommandBuffer(buffers.get(i), this.device);
                this.imageAvailable[i] = this.semaphore(stack);
                final LongBuffer fence = stack.mallocLong(1);
                check(vkCreateFence(this.device, VkFenceCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                        .flags(VK_FENCE_CREATE_SIGNALED_BIT), null, fence), "create a fence");
                this.inFlight[i] = fence.get(0);
            }
        }
    }

    private long semaphore(final MemoryStack stack) {
        final LongBuffer semaphore = stack.mallocLong(1);
        check(vkCreateSemaphore(this.device, VkSemaphoreCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO), null, semaphore), "create a semaphore");
        return semaphore.get(0);
    }

    private void barrier(final VkCommandBuffer commands, final MemoryStack stack, final long image, final int from,
                         final int to, final int sourceAccess, final int destinationAccess, final int sourceStage,
                         final int destinationStage) {
        final VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack)
                .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .oldLayout(from)
                .newLayout(to)
                .srcAccessMask(sourceAccess)
                .dstAccessMask(destinationAccess)
                .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                .image(image);
        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).levelCount(1).layerCount(1);
        vkCmdPipelineBarrier(commands, sourceStage, destinationStage, 0, null, null, barrier);
    }

    private void destroySwapchain() {
        this.destroyKept();
        for (int i = 0; i < this.views.length; i++) {
            vkDestroyImageView(this.device, this.views[i], null);
            vkDestroySemaphore(this.device, this.renderFinished[i], null);
        }
        if (this.swapchain != 0L) {
            vkDestroySwapchainKHR(this.device, this.swapchain, null);
            this.swapchain = 0L;
        }
    }

    private void destroy() {
        if (this.device != null) {
            vkDeviceWaitIdle(this.device);
            if (this.glfw != null) {
                this.glfw.close();
            }
            if (this.strata != null) {
                this.strata.close();
            }
            if (this.renderer != null) {
                this.renderer.close();
            }
            if (this.inFlight != null) {
                for (int i = 0; i < FRAMES_IN_FLIGHT; i++) {
                    vkDestroyFence(this.device, this.inFlight[i], null);
                    vkDestroySemaphore(this.device, this.imageAvailable[i], null);
                }
                vkDestroyCommandPool(this.device, this.commandPool, null);
            }
            if (this.views != null) {
                this.destroySwapchain();
            }
            vkDestroyDevice(this.device, null);
        }
        if (this.instance != null) {
            if (this.surface != 0L) {
                vkDestroySurfaceKHR(this.instance, this.surface, null);
            }
            vkDestroyInstance(this.instance, null);
        }
        if (this.window != 0L) {
            glfwDestroyWindow(this.window);
        }
    }

    private static boolean hasExtension(final MemoryStack stack, final VkPhysicalDevice device, final String name) {
        // A device can list hundreds of extensions, more than the thread's stack holds, so they go on the heap
        final IntBuffer count = stack.mallocInt(1);
        vkEnumerateDeviceExtensionProperties(device, (String) null, count, null);
        try (VkExtensionProperties.Buffer extensions = VkExtensionProperties.malloc(count.get(0))) {
            vkEnumerateDeviceExtensionProperties(device, (String) null, count, extensions);
            for (final VkExtensionProperties extension : extensions) {
                if (extension.extensionNameString().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static void check(final int result, final String action) {
        if (result != VK_SUCCESS) {
            throw new IllegalStateException("Vulkan failed to " + action + ": " + result);
        }
    }
}
