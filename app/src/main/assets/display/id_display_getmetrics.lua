function after(hook, param)
    local height = param:getSettingInt("display.height", 3100)
    local width = param:getSettingInt("display.width", 1400)
    local densityDpi = param:getSettingInt("display.density.dpi", 560)
    if height == nil or width == nil or densityDpi == nil then
        return false
    end

    local displayMetrics = param:getArgument(0)
    if displayMetrics == nil then
        return false
    end

    displayMetrics.heightPixels = height
    displayMetrics.widthPixels = width
    displayMetrics.densityDpi = densityDpi
    displayMetrics.density = densityDpi / 160.0
    displayMetrics.scaledDensity = densityDpi / 160.0
    return true, "N/A", tostring(height) .. "x" .. tostring(width)
end
