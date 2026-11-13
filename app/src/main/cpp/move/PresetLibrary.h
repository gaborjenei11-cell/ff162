#pragma once
#include <array>
#include <cstddef>
#include <string_view>

namespace gsamove {
enum class Category : int { Chaos, Stutter, Transition, Gentle, Hardcore };
struct Preset {
    const char* name;
    Category category;
    int flavor; // recipe topology/motion variant, not a displayed control
    float beats; // quarter-note multiples
    float feedback;
    float reverb;
    float brightness;
    float drive;
    float crush;
    float width;
    float motion;
    float tail;
};
inline constexpr std::array<const char*, 5> categoryNames {{"CHAOS", "STUTTER", "TRANSITION", "GENTLE", "HARDCORE"}};
inline constexpr std::array<Preset, 85> presets {{
    {"Evolving Space", Category::Chaos, 0, 0.25000f, 0.46000f, 0.38000f, 0.12000f, 0.08000f, 0.02000f, 0.55000f, 0.25000f, 0.35000f},
    {"Echo Vortex", Category::Chaos, 7, 0.75000f, 0.52700f, 0.44400f, 0.22300f, 0.16000f, 0.06500f, 0.65500f, 0.34000f, 0.44000f},
    {"Nebula Bend", Category::Chaos, 2, 2.00000f, 0.59400f, 0.50800f, 0.32600f, 0.24000f, 0.11000f, 0.76000f, 0.43000f, 0.53000f},
    {"Liquid Orbit", Category::Chaos, 9, 0.37500f, 0.66100f, 0.57200f, 0.42900f, 0.32000f, 0.15500f, 0.86500f, 0.52000f, 0.62000f},
    {"Solar Mirage", Category::Chaos, 4, 1.00000f, 0.72800f, 0.63600f, 0.53200f, 0.40000f, 0.02000f, 0.97000f, 0.61000f, 0.71000f},
    {"Prism Tunnel", Category::Chaos, 11, 3.00000f, 0.79500f, 0.70000f, 0.63500f, 0.08000f, 0.06500f, 0.55000f, 0.70000f, 0.80000f},
    {"Cloud Fracture", Category::Chaos, 6, 0.50000f, 0.46000f, 0.76400f, 0.73800f, 0.16000f, 0.11000f, 0.65500f, 0.79000f, 0.35000f},
    {"Infinite Spiral", Category::Chaos, 1, 1.50000f, 0.52700f, 0.38000f, 0.84100f, 0.24000f, 0.15500f, 0.76000f, 0.88000f, 0.44000f},
    {"Cosmic Tide", Category::Chaos, 8, 0.25000f, 0.59400f, 0.44400f, 0.12000f, 0.32000f, 0.02000f, 0.86500f, 0.25000f, 0.53000f},
    {"Doppler Garden", Category::Chaos, 3, 0.75000f, 0.66100f, 0.50800f, 0.22300f, 0.40000f, 0.06500f, 0.97000f, 0.34000f, 0.62000f},
    {"Spectral River", Category::Chaos, 10, 2.00000f, 0.72800f, 0.57200f, 0.32600f, 0.08000f, 0.11000f, 0.55000f, 0.43000f, 0.71000f},
    {"Warped Reflection", Category::Chaos, 5, 0.37500f, 0.79500f, 0.63600f, 0.42900f, 0.16000f, 0.15500f, 0.65500f, 0.52000f, 0.80000f},
    {"Polar Current", Category::Chaos, 0, 1.00000f, 0.46000f, 0.70000f, 0.53200f, 0.24000f, 0.02000f, 0.76000f, 0.61000f, 0.35000f},
    {"Luminous Drift", Category::Chaos, 7, 3.00000f, 0.52700f, 0.76400f, 0.63500f, 0.32000f, 0.06500f, 0.86500f, 0.70000f, 0.44000f},
    {"Fractal Pulse", Category::Chaos, 2, 0.50000f, 0.59400f, 0.38000f, 0.73800f, 0.40000f, 0.11000f, 0.97000f, 0.79000f, 0.53000f},
    {"Parallel Echo", Category::Chaos, 9, 1.50000f, 0.66100f, 0.44400f, 0.84100f, 0.08000f, 0.15500f, 0.55000f, 0.88000f, 0.62000f},
    {"Aurora Passage", Category::Chaos, 4, 0.25000f, 0.72800f, 0.50800f, 0.12000f, 0.16000f, 0.02000f, 0.65500f, 0.25000f, 0.71000f},
    {"Deep Mirage", Category::Chaos, 11, 0.75000f, 0.79500f, 0.57200f, 0.22300f, 0.24000f, 0.06500f, 0.76000f, 0.34000f, 0.80000f},
    {"Magnetic Void", Category::Chaos, 6, 2.00000f, 0.46000f, 0.63600f, 0.32600f, 0.32000f, 0.11000f, 0.86500f, 0.43000f, 0.35000f},
    {"Quantum Rain", Category::Chaos, 1, 0.37500f, 0.52700f, 0.70000f, 0.42900f, 0.40000f, 0.15500f, 0.97000f, 0.52000f, 0.44000f},
    {"Slice Engine", Category::Stutter, 3, 0.25000f, 0.25000f, 0.10000f, 0.40000f, 0.04000f, 0.00000f, 0.55000f, 0.20000f, 0.17000f},
    {"Triplet Twitch", Category::Stutter, 10, 0.33330f, 0.36000f, 0.16500f, 0.49500f, 0.09000f, 0.03000f, 0.66000f, 0.38000f, 0.26000f},
    {"Broken Quarter", Category::Stutter, 5, 0.12500f, 0.47000f, 0.23000f, 0.59000f, 0.14000f, 0.06000f, 0.77000f, 0.56000f, 0.35000f},
    {"Reverse Cut", Category::Stutter, 0, 0.06250f, 0.58000f, 0.29500f, 0.68500f, 0.19000f, 0.09000f, 0.88000f, 0.74000f, 0.44000f},
    {"Pulse Guillotine", Category::Stutter, 7, 0.50000f, 0.69000f, 0.36000f, 0.78000f, 0.24000f, 0.12000f, 0.99000f, 0.92000f, 0.53000f},
    {"Skyline Riser", Category::Transition, 6, 0.12500f, 0.32000f, 0.44000f, 0.13000f, 0.12000f, 0.01500f, 0.65000f, 0.38000f, 0.42000f},
    {"Reverse Gravity", Category::Transition, 1, 1.50000f, 0.37500f, 0.51800f, 0.22200f, 0.19200f, 0.05000f, 0.73000f, 0.46200f, 0.50600f},
    {"Impact Curtain", Category::Transition, 8, 0.37500f, 0.43000f, 0.59600f, 0.31400f, 0.26400f, 0.08500f, 0.81000f, 0.54400f, 0.59200f},
    {"Jetstream Lift", Category::Transition, 3, 3.00000f, 0.48500f, 0.67400f, 0.40600f, 0.33600f, 0.12000f, 0.89000f, 0.62600f, 0.67800f},
    {"Whiteout Drop", Category::Transition, 10, 1.00000f, 0.54000f, 0.75200f, 0.49800f, 0.40800f, 0.15500f, 0.97000f, 0.70800f, 0.76400f},
    {"Horizon Sweep", Category::Transition, 5, 0.25000f, 0.59500f, 0.83000f, 0.59000f, 0.48000f, 0.19000f, 0.65000f, 0.79000f, 0.85000f},
    {"Massive Uplift", Category::Transition, 0, 2.00000f, 0.65000f, 0.44000f, 0.68200f, 0.55200f, 0.01500f, 0.73000f, 0.87200f, 0.42000f},
    {"Vacuum Fall", Category::Transition, 7, 0.50000f, 0.70500f, 0.51800f, 0.77400f, 0.12000f, 0.05000f, 0.81000f, 0.38000f, 0.50600f},
    {"Cinematic Hit", Category::Transition, 2, 0.12500f, 0.32000f, 0.59600f, 0.86600f, 0.19200f, 0.08500f, 0.89000f, 0.46200f, 0.59200f},
    {"Signal Bloom", Category::Transition, 9, 1.50000f, 0.37500f, 0.67400f, 0.13000f, 0.26400f, 0.12000f, 0.97000f, 0.54400f, 0.67800f},
    {"Reverse Cascade", Category::Transition, 4, 0.37500f, 0.43000f, 0.75200f, 0.22200f, 0.33600f, 0.15500f, 0.65000f, 0.62600f, 0.76400f},
    {"Tension Bridge", Category::Transition, 11, 3.00000f, 0.48500f, 0.83000f, 0.31400f, 0.40800f, 0.19000f, 0.73000f, 0.70800f, 0.85000f},
    {"Vertical Rush", Category::Transition, 6, 1.00000f, 0.54000f, 0.44000f, 0.40600f, 0.48000f, 0.01500f, 0.81000f, 0.79000f, 0.42000f},
    {"Aurora Drop", Category::Transition, 1, 0.25000f, 0.59500f, 0.51800f, 0.49800f, 0.55200f, 0.05000f, 0.89000f, 0.87200f, 0.50600f},
    {"Pressure Wave", Category::Transition, 8, 2.00000f, 0.65000f, 0.59600f, 0.59000f, 0.12000f, 0.08500f, 0.97000f, 0.38000f, 0.59200f},
    {"Airlock Spin", Category::Transition, 3, 0.50000f, 0.70500f, 0.67400f, 0.68200f, 0.19200f, 0.12000f, 0.65000f, 0.46200f, 0.67800f},
    {"Time Collapse", Category::Transition, 10, 0.12500f, 0.32000f, 0.75200f, 0.77400f, 0.26400f, 0.15500f, 0.73000f, 0.54400f, 0.76400f},
    {"Thunder Fade", Category::Transition, 5, 1.50000f, 0.37500f, 0.83000f, 0.86600f, 0.33600f, 0.19000f, 0.81000f, 0.62600f, 0.85000f},
    {"Neon Liftoff", Category::Transition, 0, 0.37500f, 0.43000f, 0.44000f, 0.13000f, 0.40800f, 0.01500f, 0.89000f, 0.70800f, 0.42000f},
    {"Final Descent", Category::Transition, 7, 3.00000f, 0.48500f, 0.51800f, 0.22200f, 0.48000f, 0.05000f, 0.97000f, 0.79000f, 0.50600f},
    {"Soft Horizon", Category::Gentle, 9, 0.50000f, 0.24000f, 0.28000f, 0.25000f, 0.01500f, 0.00000f, 0.42000f, 0.08000f, 0.35000f},
    {"Velvet Echo", Category::Gentle, 4, 3.00000f, 0.28700f, 0.33500f, 0.33500f, 0.04300f, 0.00000f, 0.49600f, 0.15100f, 0.42400f},
    {"Dream Glass", Category::Gentle, 11, 2.00000f, 0.33400f, 0.39000f, 0.42000f, 0.07100f, 0.00000f, 0.57200f, 0.22200f, 0.49800f},
    {"Warm Tides", Category::Gentle, 6, 1.50000f, 0.38100f, 0.44500f, 0.50500f, 0.09900f, 0.00000f, 0.64800f, 0.29300f, 0.57200f},
    {"Feather Orbit", Category::Gentle, 1, 1.00000f, 0.42800f, 0.50000f, 0.59000f, 0.01500f, 0.00000f, 0.72400f, 0.36400f, 0.64600f},
    {"Silk Drift", Category::Gentle, 8, 0.75000f, 0.47500f, 0.55500f, 0.67500f, 0.04300f, 0.00000f, 0.80000f, 0.43500f, 0.72000f},
    {"Gentle Bloom", Category::Gentle, 3, 0.50000f, 0.52200f, 0.61000f, 0.76000f, 0.07100f, 0.00000f, 0.87600f, 0.08000f, 0.79400f},
    {"Afterglow", Category::Gentle, 10, 3.00000f, 0.24000f, 0.66500f, 0.25000f, 0.09900f, 0.00000f, 0.42000f, 0.15100f, 0.35000f},
    {"Silver Mist", Category::Gentle, 5, 2.00000f, 0.28700f, 0.28000f, 0.33500f, 0.01500f, 0.00000f, 0.49600f, 0.22200f, 0.42400f},
    {"Low Moon", Category::Gentle, 0, 1.50000f, 0.33400f, 0.33500f, 0.42000f, 0.04300f, 0.00000f, 0.57200f, 0.29300f, 0.49800f},
    {"Floating Dust", Category::Gentle, 7, 1.00000f, 0.38100f, 0.39000f, 0.50500f, 0.07100f, 0.00000f, 0.64800f, 0.36400f, 0.57200f},
    {"Cloud Ribbon", Category::Gentle, 2, 0.75000f, 0.42800f, 0.44500f, 0.59000f, 0.09900f, 0.00000f, 0.72400f, 0.43500f, 0.64600f},
    {"Pastel Motion", Category::Gentle, 9, 0.50000f, 0.47500f, 0.50000f, 0.67500f, 0.01500f, 0.00000f, 0.80000f, 0.08000f, 0.72000f},
    {"Quiet Matter", Category::Gentle, 4, 3.00000f, 0.52200f, 0.55500f, 0.76000f, 0.04300f, 0.00000f, 0.87600f, 0.15100f, 0.79400f},
    {"Ocean Halo", Category::Gentle, 11, 2.00000f, 0.24000f, 0.61000f, 0.25000f, 0.07100f, 0.00000f, 0.42000f, 0.22200f, 0.35000f},
    {"Slow Aurora", Category::Gentle, 6, 1.50000f, 0.28700f, 0.66500f, 0.33500f, 0.09900f, 0.00000f, 0.49600f, 0.29300f, 0.42400f},
    {"Thin Air", Category::Gentle, 1, 1.00000f, 0.33400f, 0.28000f, 0.42000f, 0.01500f, 0.00000f, 0.57200f, 0.36400f, 0.49800f},
    {"Soft Focus", Category::Gentle, 8, 0.75000f, 0.38100f, 0.33500f, 0.50500f, 0.04300f, 0.00000f, 0.64800f, 0.43500f, 0.57200f},
    {"Distant Memory", Category::Gentle, 3, 0.50000f, 0.42800f, 0.39000f, 0.59000f, 0.07100f, 0.00000f, 0.72400f, 0.08000f, 0.64600f},
    {"Boreal Light", Category::Gentle, 10, 3.00000f, 0.47500f, 0.44500f, 0.67500f, 0.09900f, 0.00000f, 0.80000f, 0.15100f, 0.72000f},
    {"Bit Furnace", Category::Hardcore, 0, 0.06250f, 0.22000f, 0.24000f, 0.13000f, 0.42000f, 0.35000f, 0.45000f, 0.42000f, 0.32000f},
    {"Digital Riot", Category::Hardcore, 7, 0.37500f, 0.28900f, 0.31000f, 0.23000f, 0.49500f, 0.44000f, 0.53800f, 0.51000f, 0.39800f},
    {"Crushed Orbit", Category::Hardcore, 2, 1.00000f, 0.35800f, 0.38000f, 0.33000f, 0.57000f, 0.53000f, 0.62600f, 0.60000f, 0.47600f},
    {"Iron Destroyer", Category::Hardcore, 9, 0.25000f, 0.42700f, 0.45000f, 0.43000f, 0.64500f, 0.62000f, 0.71400f, 0.69000f, 0.55400f},
    {"Rust Driver", Category::Hardcore, 4, 0.75000f, 0.49600f, 0.52000f, 0.53000f, 0.72000f, 0.71000f, 0.80200f, 0.78000f, 0.63200f},
    {"Shattered Clock", Category::Hardcore, 11, 0.12500f, 0.56500f, 0.59000f, 0.63000f, 0.79500f, 0.80000f, 0.89000f, 0.87000f, 0.71000f},
    {"Voltage Burn", Category::Hardcore, 6, 0.50000f, 0.63400f, 0.24000f, 0.73000f, 0.87000f, 0.89000f, 0.97800f, 0.42000f, 0.78800f},
    {"Dirt Machine", Category::Hardcore, 1, 0.06250f, 0.70300f, 0.31000f, 0.83000f, 0.94500f, 0.35000f, 0.45000f, 0.51000f, 0.32000f},
    {"Pixel Damage", Category::Hardcore, 8, 0.37500f, 0.22000f, 0.38000f, 0.13000f, 0.42000f, 0.44000f, 0.53800f, 0.60000f, 0.39800f},
    {"Signal Punisher", Category::Hardcore, 3, 1.00000f, 0.28900f, 0.45000f, 0.23000f, 0.49500f, 0.53000f, 0.62600f, 0.69000f, 0.47600f},
    {"Toxic Stutter", Category::Hardcore, 10, 0.25000f, 0.35800f, 0.52000f, 0.33000f, 0.57000f, 0.62000f, 0.71400f, 0.78000f, 0.55400f},
    {"Razor Echo", Category::Hardcore, 5, 0.75000f, 0.42700f, 0.59000f, 0.43000f, 0.64500f, 0.71000f, 0.80200f, 0.87000f, 0.63200f},
    {"Fracture Drive", Category::Hardcore, 0, 0.12500f, 0.49600f, 0.24000f, 0.53000f, 0.72000f, 0.80000f, 0.89000f, 0.42000f, 0.71000f},
    {"Steel Meltdown", Category::Hardcore, 7, 0.50000f, 0.56500f, 0.31000f, 0.63000f, 0.79500f, 0.89000f, 0.97800f, 0.51000f, 0.78800f},
    {"Granite Shred", Category::Hardcore, 2, 0.06250f, 0.63400f, 0.38000f, 0.73000f, 0.87000f, 0.35000f, 0.45000f, 0.60000f, 0.32000f},
    {"Broken Codec", Category::Hardcore, 9, 0.37500f, 0.70300f, 0.45000f, 0.83000f, 0.94500f, 0.44000f, 0.53800f, 0.69000f, 0.39800f},
    {"Overdrive Storm", Category::Hardcore, 4, 1.00000f, 0.22000f, 0.52000f, 0.13000f, 0.42000f, 0.53000f, 0.62600f, 0.78000f, 0.47600f},
    {"Brutal Bitwave", Category::Hardcore, 11, 0.25000f, 0.28900f, 0.59000f, 0.23000f, 0.49500f, 0.62000f, 0.71400f, 0.87000f, 0.55400f},
    {"Industrial Sink", Category::Hardcore, 6, 0.75000f, 0.35800f, 0.24000f, 0.33000f, 0.57000f, 0.71000f, 0.80200f, 0.42000f, 0.63200f},
    {"Zero Mercy", Category::Hardcore, 1, 0.12500f, 0.42700f, 0.31000f, 0.43000f, 0.64500f, 0.80000f, 0.89000f, 0.51000f, 0.71000f},
}};
constexpr int categoryStart(Category c) noexcept {
    switch(c) { case Category::Chaos: return 0; case Category::Stutter: return 20;
        case Category::Transition: return 25; case Category::Gentle: return 45;
        case Category::Hardcore: return 65; }
    return 0;
}
constexpr int categoryCount(Category c) noexcept { return c == Category::Stutter ? 5 : 20; }
constexpr Category categoryForPreset(int index) noexcept {
    return index < 20 ? Category::Chaos : index < 25 ? Category::Stutter
        : index < 45 ? Category::Transition : index < 65 ? Category::Gentle : Category::Hardcore;
}
constexpr int clampPreset(int index) noexcept { return index < 0 ? 0 : (index > 84 ? 84 : index); }

// Preset-specific internal XY recipes. X, Y, and their combined position
// drive three independent DSP roles; there are NO user-facing macro parameters.
enum class EffectRole : unsigned char {
    DelayTime, Feedback, Reverb, PitchMotion, Drive, Crush, Width, Granular, Chorus, StutterRate, Gate, Decay, Riser, Impact, Timbre, Flutter, PitchCloud, ReverseGrain
};
struct EffectProfile {
    std::array<EffectRole,3> roles;
    float intensity;
    float character;
};
inline constexpr std::array<const char*,18> effectRoleNames {{
    "Time Stretch",
    "Echo Regeneration",
    "Space Bloom",
    "Pitch Motion",
    "Harmonic Drive",
    "Bit Crusher",
    "Stereo Width",
    "Grain Density",
    "Chorus Motion",
    "Repeat Rate",
    "Rhythmic Gate",
    "Reverb Tail",
    "Riser Motion",
    "Transient Impact",
    "Harmonic Texture",
    "Tape Flutter",
    "Shimmer Cloud",
    "Reverse Grain"
}};
inline constexpr std::array<unsigned char,18> effectEngineMasks {{
0x02, 0x02, 0x04, 0x12, 0x08, 0x08, 0x10, 0x20, 0x10, 0x20, 0x20, 0x04, 0x22, 0x0a, 0x18, 0x12, 0x24, 0x22
}};
inline constexpr std::array<EffectProfile,85> effectProfiles {{
    {{EffectRole::DelayTime, EffectRole::Reverb, EffectRole::Width}, 0.80f, 0.80f}, // 00 Evolving Space,
    {{EffectRole::PitchMotion, EffectRole::Riser, EffectRole::DelayTime}, 0.90f, 0.96f}, // 01 Echo Vortex,
    {{EffectRole::Chorus, EffectRole::Feedback, EffectRole::Riser}, 1.00f, 1.12f}, // 02 Nebula Bend,
    {{EffectRole::Decay, EffectRole::Chorus, EffectRole::PitchMotion}, 1.10f, 0.80f}, // 03 Liquid Orbit,
    {{EffectRole::Feedback, EffectRole::DelayTime, EffectRole::PitchCloud}, 1.20f, 0.96f}, // 04 Solar Mirage,
    {{EffectRole::Width, EffectRole::Granular, EffectRole::Chorus}, 0.80f, 1.12f}, // 05 Prism Tunnel,
    {{EffectRole::Riser, EffectRole::PitchCloud, EffectRole::Reverb}, 0.90f, 0.80f}, // 06 Cloud Fracture,
    {{EffectRole::PitchCloud, EffectRole::Width, EffectRole::Decay}, 1.00f, 0.96f}, // 07 Infinite Spiral,
    {{EffectRole::Reverb, EffectRole::Decay, EffectRole::Granular}, 1.10f, 1.12f}, // 08 Cosmic Tide,
    {{EffectRole::Granular, EffectRole::PitchMotion, EffectRole::Feedback}, 1.20f, 0.80f}, // 09 Doppler Garden,
    {{EffectRole::Flutter, EffectRole::DelayTime, EffectRole::Feedback}, 0.80f, 0.96f}, // 10 Spectral River,
    {{EffectRole::DelayTime, EffectRole::Reverb, EffectRole::Feedback}, 0.90f, 1.12f}, // 11 Warped Reflection,
    {{EffectRole::PitchMotion, EffectRole::Riser, EffectRole::Feedback}, 1.00f, 0.80f}, // 12 Polar Current,
    {{EffectRole::Chorus, EffectRole::Feedback, EffectRole::DelayTime}, 1.10f, 0.96f}, // 13 Luminous Drift,
    {{EffectRole::Decay, EffectRole::Chorus, EffectRole::DelayTime}, 1.20f, 1.12f}, // 14 Fractal Pulse,
    {{EffectRole::Feedback, EffectRole::DelayTime, EffectRole::Reverb}, 0.80f, 0.80f}, // 15 Parallel Echo,
    {{EffectRole::Width, EffectRole::Granular, EffectRole::DelayTime}, 0.90f, 0.96f}, // 16 Aurora Passage,
    {{EffectRole::Riser, EffectRole::PitchCloud, EffectRole::DelayTime}, 1.00f, 1.12f}, // 17 Deep Mirage,
    {{EffectRole::PitchCloud, EffectRole::Width, EffectRole::DelayTime}, 1.10f, 0.80f}, // 18 Magnetic Void,
    {{EffectRole::Reverb, EffectRole::Decay, EffectRole::DelayTime}, 1.20f, 0.96f}, // 19 Quantum Rain,
    {{EffectRole::Gate, EffectRole::Feedback, EffectRole::DelayTime}, 0.80f, 0.80f}, // 20 Slice Engine,
    {{EffectRole::Crush, EffectRole::StutterRate, EffectRole::Gate}, 0.90f, 0.96f}, // 21 Triplet Twitch,
    {{EffectRole::Reverb, EffectRole::DelayTime, EffectRole::Feedback}, 1.00f, 1.12f}, // 22 Broken Quarter,
    {{EffectRole::ReverseGrain, EffectRole::StutterRate, EffectRole::Gate}, 1.10f, 0.80f}, // 23 Reverse Cut,
    {{EffectRole::DelayTime, EffectRole::Reverb, EffectRole::Gate}, 1.20f, 0.96f}, // 24 Pulse Guillotine,
    {{EffectRole::Reverb, EffectRole::Feedback, EffectRole::PitchCloud}, 0.80f, 0.80f}, // 25 Skyline Riser,
    {{EffectRole::Granular, EffectRole::Width, EffectRole::Decay}, 0.90f, 0.96f}, // 26 Reverse Gravity,
    {{EffectRole::ReverseGrain, EffectRole::Feedback, EffectRole::Riser}, 1.00f, 1.12f}, // 27 Impact Curtain,
    {{EffectRole::Impact, EffectRole::Width, EffectRole::PitchMotion}, 1.10f, 0.80f}, // 28 Jetstream Lift,
    {{EffectRole::Feedback, EffectRole::Riser, EffectRole::Impact}, 1.20f, 0.96f}, // 29 Whiteout Drop,
    {{EffectRole::PitchMotion, EffectRole::Width, EffectRole::Impact}, 0.80f, 1.12f}, // 30 Horizon Sweep,
    {{EffectRole::Riser, EffectRole::Feedback, EffectRole::ReverseGrain}, 0.90f, 0.80f}, // 31 Massive Uplift,
    {{EffectRole::Decay, EffectRole::Width, EffectRole::Granular}, 1.00f, 0.96f}, // 32 Vacuum Fall,
    {{EffectRole::PitchCloud, EffectRole::Feedback, EffectRole::Reverb}, 1.10f, 1.12f}, // 33 Cinematic Hit,
    {{EffectRole::Width, EffectRole::Riser, EffectRole::Impact}, 1.20f, 0.80f}, // 34 Signal Bloom,
    {{EffectRole::Reverb, EffectRole::Feedback, EffectRole::Riser}, 0.80f, 0.96f}, // 35 Reverse Cascade,
    {{EffectRole::Granular, EffectRole::Width, EffectRole::Riser}, 0.90f, 1.12f}, // 36 Tension Bridge,
    {{EffectRole::ReverseGrain, EffectRole::Feedback, EffectRole::Impact}, 1.00f, 0.80f}, // 37 Vertical Rush,
    {{EffectRole::Impact, EffectRole::Width, EffectRole::Riser}, 1.10f, 0.96f}, // 38 Aurora Drop,
    {{EffectRole::Feedback, EffectRole::Riser, EffectRole::Reverb}, 1.20f, 1.12f}, // 39 Pressure Wave,
    {{EffectRole::PitchMotion, EffectRole::Width, EffectRole::Riser}, 0.80f, 0.80f}, // 40 Airlock Spin,
    {{EffectRole::Riser, EffectRole::Feedback, EffectRole::Impact}, 0.90f, 0.96f}, // 41 Time Collapse,
    {{EffectRole::Decay, EffectRole::Width, EffectRole::Riser}, 1.00f, 1.12f}, // 42 Thunder Fade,
    {{EffectRole::PitchCloud, EffectRole::Feedback, EffectRole::Riser}, 1.10f, 0.80f}, // 43 Neon Liftoff,
    {{EffectRole::Width, EffectRole::Riser, EffectRole::Reverb}, 1.20f, 0.96f}, // 44 Final Descent,
    {{EffectRole::Chorus, EffectRole::DelayTime, EffectRole::PitchMotion}, 0.80f, 0.80f}, // 45 Soft Horizon,
    {{EffectRole::Decay, EffectRole::Timbre, EffectRole::Flutter}, 0.90f, 0.96f}, // 46 Velvet Echo,
    {{EffectRole::PitchCloud, EffectRole::DelayTime, EffectRole::Width}, 1.00f, 1.12f}, // 47 Dream Glass,
    {{EffectRole::Reverb, EffectRole::Timbre, EffectRole::Granular}, 1.10f, 0.80f}, // 48 Warm Tides,
    {{EffectRole::DelayTime, EffectRole::Timbre, EffectRole::Width}, 1.20f, 0.96f}, // 49 Feather Orbit,
    {{EffectRole::Granular, EffectRole::Timbre, EffectRole::Reverb}, 0.80f, 1.12f}, // 50 Silk Drift,
    {{EffectRole::Width, EffectRole::DelayTime, EffectRole::PitchCloud}, 0.90f, 0.80f}, // 51 Gentle Bloom,
    {{EffectRole::Flutter, EffectRole::Timbre, EffectRole::Decay}, 1.00f, 0.96f}, // 52 Afterglow,
    {{EffectRole::PitchMotion, EffectRole::DelayTime, EffectRole::Chorus}, 1.10f, 1.12f}, // 53 Silver Mist,
    {{EffectRole::Timbre, EffectRole::Width, EffectRole::Reverb}, 1.20f, 0.80f}, // 54 Low Moon,
    {{EffectRole::Chorus, EffectRole::DelayTime, EffectRole::Timbre}, 0.80f, 0.96f}, // 55 Floating Dust,
    {{EffectRole::Decay, EffectRole::Timbre, EffectRole::Width}, 0.90f, 1.12f}, // 56 Cloud Ribbon,
    {{EffectRole::PitchCloud, EffectRole::DelayTime, EffectRole::Timbre}, 1.00f, 0.80f}, // 57 Pastel Motion,
    {{EffectRole::Reverb, EffectRole::Timbre, EffectRole::Width}, 1.10f, 0.96f}, // 58 Quiet Matter,
    {{EffectRole::DelayTime, EffectRole::Timbre, EffectRole::Reverb}, 1.20f, 1.12f}, // 59 Ocean Halo,
    {{EffectRole::Granular, EffectRole::Timbre, EffectRole::Width}, 0.80f, 0.80f}, // 60 Slow Aurora,
    {{EffectRole::Width, EffectRole::DelayTime, EffectRole::Timbre}, 0.90f, 0.96f}, // 61 Thin Air,
    {{EffectRole::Flutter, EffectRole::Timbre, EffectRole::Width}, 1.00f, 1.12f}, // 62 Soft Focus,
    {{EffectRole::PitchMotion, EffectRole::DelayTime, EffectRole::Timbre}, 1.10f, 0.80f}, // 63 Distant Memory,
    {{EffectRole::Timbre, EffectRole::Width, EffectRole::Chorus}, 1.20f, 0.96f}, // 64 Boreal Light,
    {{EffectRole::Granular, EffectRole::StutterRate, EffectRole::ReverseGrain}, 0.80f, 0.80f}, // 65 Bit Furnace,
    {{EffectRole::Impact, EffectRole::Drive, EffectRole::PitchMotion}, 0.90f, 0.96f}, // 66 Digital Riot,
    {{EffectRole::Crush, EffectRole::StutterRate, EffectRole::Feedback}, 1.00f, 1.12f}, // 67 Crushed Orbit,
    {{EffectRole::Gate, EffectRole::Drive, EffectRole::Width}, 1.10f, 0.80f}, // 68 Iron Destroyer,
    {{EffectRole::StutterRate, EffectRole::Crush, EffectRole::Drive}, 1.20f, 0.96f}, // 69 Rust Driver,
    {{EffectRole::Width, EffectRole::Drive, EffectRole::Gate}, 0.80f, 1.12f}, // 70 Shattered Clock,
    {{EffectRole::Feedback, EffectRole::StutterRate, EffectRole::Crush}, 0.90f, 0.80f}, // 71 Voltage Burn,
    {{EffectRole::PitchMotion, EffectRole::Drive, EffectRole::Impact}, 1.00f, 0.96f}, // 72 Dirt Machine,
    {{EffectRole::ReverseGrain, EffectRole::StutterRate, EffectRole::Granular}, 1.10f, 1.12f}, // 73 Pixel Damage,
    {{EffectRole::Drive, EffectRole::Crush, EffectRole::Feedback}, 1.20f, 0.80f}, // 74 Signal Punisher,
    {{EffectRole::Granular, EffectRole::StutterRate, EffectRole::Crush}, 0.80f, 0.96f}, // 75 Toxic Stutter,
    {{EffectRole::Impact, EffectRole::Drive, EffectRole::Crush}, 0.90f, 1.12f}, // 76 Razor Echo,
    {{EffectRole::Crush, EffectRole::StutterRate, EffectRole::Drive}, 1.00f, 0.80f}, // 77 Fracture Drive,
    {{EffectRole::Gate, EffectRole::Drive, EffectRole::Crush}, 1.10f, 0.96f}, // 78 Steel Meltdown,
    {{EffectRole::StutterRate, EffectRole::Crush, EffectRole::Feedback}, 1.20f, 1.12f}, // 79 Granite Shred,
    {{EffectRole::Width, EffectRole::Drive, EffectRole::Crush}, 0.80f, 0.80f}, // 80 Broken Codec,
    {{EffectRole::Feedback, EffectRole::StutterRate, EffectRole::Drive}, 0.90f, 0.96f}, // 81 Overdrive Storm,
    {{EffectRole::PitchMotion, EffectRole::Drive, EffectRole::Crush}, 1.00f, 1.12f}, // 82 Brutal Bitwave,
    {{EffectRole::ReverseGrain, EffectRole::StutterRate, EffectRole::Crush}, 1.10f, 0.80f}, // 83 Industrial Sink,
    {{EffectRole::Drive, EffectRole::Crush, EffectRole::Gate}, 1.20f, 0.96f} // 84 Zero Mercy
}};
inline constexpr const EffectProfile& profileFor(int index) noexcept {
    return effectProfiles[static_cast<size_t>(clampPreset(index))];
}

inline constexpr std::array<const char*,9> skinNames {{
    "Dark Graphite Metal", "Cream Brushed Metal", "Blue Alloy", "Walnut Glass",
    "Violet Titanium", "Solar Gold", "Gunmetal Amber", "Bronze Cyan", "Pearl Magenta"
}};
static_assert(presets.size() == 85, "85 distinct programs expected");
static_assert(categoryStart(Category::Hardcore) + categoryCount(Category::Hardcore) == 85);
} // namespace gsamove
