import os
from PIL import Image

# Path to the uploaded image
source_image_path = r"C:\Users\prem7\.gemini\antigravity-ide\brain\f32afdb3-c485-4000-a924-bccfbc02dd0c\media__1780419733637.jpg"

# Path to the res directory
res_dir = r"C:\Users\prem7\AndroidStudioProjects\One Line A Day\app\src\main\res"

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

# The foreground image for adaptive icons needs to be larger (108dp * 108dp), roughly 432x432 for xxxhdpi
adaptive_foreground_sizes = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}

def resize_and_save(img, size, path):
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(path, format="PNG")
    print(f"Saved {path}")

try:
    img = Image.open(source_image_path).convert("RGBA")
    
    for density, size in sizes.items():
        mipmap_dir = os.path.join(res_dir, f"mipmap-{density}")
        os.makedirs(mipmap_dir, exist_ok=True)
        
        # Save ic_launcher.png and ic_launcher_round.png
        resize_and_save(img, size, os.path.join(mipmap_dir, "ic_launcher.png"))
        resize_and_save(img, size, os.path.join(mipmap_dir, "ic_launcher_round.png"))
        
    for density, size in adaptive_foreground_sizes.items():
        mipmap_dir = os.path.join(res_dir, f"mipmap-{density}")
        os.makedirs(mipmap_dir, exist_ok=True)
        # Save foreground for adaptive icon
        resize_and_save(img, size, os.path.join(mipmap_dir, "ic_launcher_foreground.png"))
        
    # Also let's clean up any .webp versions that might take precedence
    for density in sizes.keys():
        for f in ["ic_launcher.webp", "ic_launcher_round.webp", "ic_launcher_foreground.webp"]:
            p = os.path.join(res_dir, f"mipmap-{density}", f)
            if os.path.exists(p):
                os.remove(p)
                print(f"Removed {p}")
                
    # Clean up the XML foreground if it exists
    xml_foreground = os.path.join(res_dir, "drawable", "ic_launcher_foreground.xml")
    if os.path.exists(xml_foreground):
        os.remove(xml_foreground)
        print(f"Removed {xml_foreground}")
        
    xml_foreground_v24 = os.path.join(res_dir, "drawable-v24", "ic_launcher_foreground.xml")
    if os.path.exists(xml_foreground_v24):
        os.remove(xml_foreground_v24)
        print(f"Removed {xml_foreground_v24}")

    print("Icon update complete!")

except Exception as e:
    print(f"Error: {e}")
