package com.laptrinhoop.controller.web;

import java.util.List;
import com.laptrinhoop.service.impl.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.laptrinhoop.dao.IProductDAO;
import com.laptrinhoop.entity.Category;
import com.laptrinhoop.entity.Product;
import com.laptrinhoop.service.ICategoryService;
import com.laptrinhoop.service.ICookieService;
import com.laptrinhoop.service.IHttpService;
import com.laptrinhoop.service.IMailService;
import com.laptrinhoop.service.IProductService;

@Controller
public class ProductController {

	private final ProductService productService;

	@Autowired
	private ICategoryService service;
	@Autowired
	private IProductDAO dao;

	@Autowired
	private IProductService serviceProduct;

	@Autowired
	private ICookieService cookieService;

	@Autowired
	private IMailService mailService;

	@Autowired
	private IHttpService httpService;

	ProductController(ProductService productService) {
		this.productService = productService;
	}

	// @Autowired
	// private IElasticSearch elastic;

	@RequestMapping("/product/list-by-category/{cId}")
	public String listByCategory(@PathVariable("cId") Integer id, Model model) {
		Category category = service.findById(id);
		List<Product> listProduct = category.getProducts();
		model.addAttribute("list", listProduct);
		return "product/list";
	}

	@RequestMapping("/product/list-by-keywords")
	public String listByKeyWords(@RequestParam("keywords") String keywords, Model model) {
		List<Product> results = productService.findByKeywords(keywords);
		model.addAttribute("list", results);
		model.addAttribute("keywords", keywords); // để hiển thị lại từ khóa tìm kiếm nếu cần
		return "product/list";
	}

	@RequestMapping("/product/detail/{id}")
	public String detail(@PathVariable("id") Integer id, Model model) {
		Product p = serviceProduct.findById(id);
		p.setViewCount(p.getViewCount() + 1);
		dao.update(p);
		model.addAttribute("prod", p);
		List<Product> listDaXem = serviceProduct.getViewProduct("daXem", id.toString());
		model.addAttribute("daXem", listDaXem);
		String ids = cookieService.getCookieValue("like", "");
		if (!ids.isEmpty()) {
			List<Product> list = dao.findByIdsInCookie(ids);
			model.addAttribute("like", list);
		}
		return "product/detail";
	}

	@ResponseBody
	@RequestMapping("/product/favorite/{id}")
	public String[] favorite(@PathVariable("id") Integer id) {
		String ids = cookieService.getCookieValue("like", id.toString());
		if (!ids.contains(id.toString())) {
			ids += "," + id.toString();
		}
		cookieService.createCookie("like", ids, 15);
		return ids.split(",");
	}

	@RequestMapping("/product/list-by-hot/{key}")
	public String listByHot(@PathVariable("key") String key, Model model) {
		List<Product> listP = serviceProduct.findByHot(key);
		model.addAttribute("list", listP);
		return "product/list";
	}

	@ResponseBody
	@RequestMapping("/product/send-friend")
	public String sendFriend(@RequestParam("id") Integer id, @RequestParam("from") String from,
			@RequestParam("to") String to, @RequestParam("subject") String subject, @RequestParam("body") String body) {
		String url = httpService.getCurrentUrl().replace("send-friend", "detail/" + id);
		mailService.send(to, subject, body + "<hr/><a href='" + url + "'>Xem chi tiết</a>");
		return "Đã gửi thông tin thành công";
	}

}
